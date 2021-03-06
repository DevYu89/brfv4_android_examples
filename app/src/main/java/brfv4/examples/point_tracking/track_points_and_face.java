package brfv4.examples.point_tracking;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Vector;

import brfv4.BRFFace;
import brfv4.BRFManager;
import brfv4.android.DrawingUtils;
import brfv4.examples.BRFBasicJavaExample;
import brfv4.geom.Point;
import brfv4.geom.Rectangle;

public class track_points_and_face extends BRFBasicJavaExample {

	Vector<Point> _pointsToAdd	= new Vector<Point>();
	int _numTrackedPoints		= 0;

	public track_points_and_face(Context context) {
		super(context);
	}

	@Override
	public void initCurrentExample(BRFManager brfManager, Rectangle resolution) {

		Log.d("BRFv4", "BRFv4 - basic - point tracking - track points and a face.\n" +
			"Click eg. on your face to add a bunch of points to track.");

		brfManager.init(resolution, resolution, _appId);

		// BRFMode.POINT_TRACKING skips the face detection/tracking entirely.
		// This examples shows that both can be done simultaneously by setting
		// the mode to BRFMode.FACE_TRACKING.

		brfManager.setMode(brfv4.BRFMode.FACE_TRACKING);

		// Default settings: a patch size of 21 (needs to be odd), 4 pyramid levels,
		// 50 iterations and a small error of 0.0006

		brfManager.setOpticalFlowParams(21, 4, 50, 0.0006);

		// true means:  BRF will remove points if they are not valid anymore.
		// false means: developers handle point removal on their own.

		brfManager.setOpticalFlowCheckPointsValidBeforeTracking(true);
	}

	@Override
	public synchronized void updateCurrentExample(BRFManager brfManager, Bitmap imageData, DrawingUtils draw) {

		// We add the _pointsToAdd right before an update.
		// If you do that onclick, the tracking might not
		// handle the new points correctly.

		if(_pointsToAdd.size() > 0) {
			brfManager.addOpticalFlowPoints(_pointsToAdd);
			_pointsToAdd.clear();
		}

		brfManager.update(imageData);

		draw.clear();

		// Face detection results: a rough rectangle used to start the face tracking.

		draw.drawRects(brfManager.getAllDetectedFaces(),	false, 1.0, 0x00a1ff, 0.5);
		draw.drawRects(brfManager.getMergedDetectedFaces(),	false, 2.0, 0xffd200, 1.0);

		// Get all faces. The default setup only tracks one face.

		Vector<BRFFace> faces = brfManager.getFaces();
		int i = 0;

		for(i = 0; i < faces.size(); i++) {

			BRFFace face = faces.get(i);

			if(		face.state.equals(brfv4.BRFState.FACE_TRACKING_START) ||
					face.state.equals(brfv4.BRFState.FACE_TRACKING)) {

				// Face tracking results: 68 facial feature points.

				draw.drawTriangles(	face.vertices, face.triangles, false, 1.0, 0x00a0ff, 0.4);
				draw.drawVertices(	face.vertices, 2.0, false, 0x00a0ff, 0.4);
			}
		}

		Vector<Point> points = brfManager.getOpticalFlowPoints();
		Vector<Boolean> states = brfManager.getOpticalFlowPointStates();

		// Draw points by state: green valid, red invalid

		for(i = 0; i < points.size(); i++) {
			if(states.get(i)) {
				draw.drawPoint(points.get(i), 2, false, 0x00ff00, 1.0);
			} else {
				draw.drawPoint(points.get(i), 2, false, 0xff0000, 1.0);
			}
		}

		// ... or just draw all points that got tracked.
//		draw.drawPoints(points, 2, false, 0x00ff00, 1.0);

		if(points.size() != _numTrackedPoints) {
			_numTrackedPoints = points.size();
			Log.d("BRFv4", "Tracking " + _numTrackedPoints + " points.");
		}
	}

	@Override
	public synchronized boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();

		if(action == MotionEvent.ACTION_DOWN) {

			float x = event.getX();
			float y = event.getY();

			// Add 1 point:

			//_pointsToAdd.add(new Point(x, y));

			//Add 100 points

			float w = 60.0f;
			float step = 6.0f;
			float xStart = x - w * 0.5f;
			float xEnd = x + w * 0.5f;
			float yStart = y - w * 0.5f;
			float yEnd = y + w * 0.5f;
			float dy = yStart;
			float dx = xStart;

			for(; dy < yEnd; dy += step) {
				for(dx = xStart; dx < xEnd; dx += step) {
					_pointsToAdd.add(new Point(dx, dy));
				}
			}
		}

		return true;
	}
}
