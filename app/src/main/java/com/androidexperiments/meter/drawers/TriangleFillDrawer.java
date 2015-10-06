package com.androidexperiments.meter.drawers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * A triangle that is filled up to a set percent
 */
public class TriangleFillDrawer extends Drawer {

    private static final boolean DEBUG_DRAW = false;

    //The label to display when tapped
    protected String label1 = "";
    protected String label2 = "";

    //Percentage of fill
    protected float percent = 0;
    protected float _percent = 0;

    // Colors
    private final int color_background;
    private final int color_triangle_background;
    private final int color_triangle_foreground;
    private final int color_triangle_critical;

    protected boolean connected = true;
    private boolean _connected;

    protected Vector2D pos;
    private Vector2D _pos;
    private Vector2D vel;

    private Paint paint = new Paint();


    private Vector2D normal = new Vector2D(0,0);
    private Vector2D sideA = new Vector2D(0,0);
    private Vector2D sideB = new Vector2D(0,0);
    private Vector2D pivot = new Vector2D(0,0);




    public TriangleFillDrawer(Context context, int colorBack, int colorTriBack, int colorTriFore, int colorTriCritical){
        super(context);
        this.color_background = colorBack;
        this.color_triangle_background = colorTriBack;
        this.color_triangle_foreground = colorTriFore;
        this.color_triangle_critical = colorTriCritical;
        this.textColor = colorTriBack;
    }

    /**
     * builds the vertices, counter-clockwise for a triangle
     * @param triangleSize
     * @return
     */
    protected Vector2D[] createTriangleVertices(float triangleSize){

        Vector2D[] p = new Vector2D[3];

        double f;

        //top-right
        f = 2.0 * Math.PI / 3.0;
        p[0] = new Vector2D(triangleSize * Math.sin(f), triangleSize * Math.cos(f));
        //top-left
        f = 4.0 * Math.PI / 3.0;
        p[1] = new Vector2D(triangleSize * Math.sin(f), triangleSize * Math.cos(f));
        //bottom
        f = 6.0 * Math.PI / 3.0;
        p[2] = new Vector2D(triangleSize * Math.sin(f), triangleSize * Math.cos(f));

        return p;
    }




    /**
     * Function that generates a triangle path
     */
    protected Path createTriangle(int x, int y, float triangleSize, float height, boolean flip){
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        triangleSize /= 2;

        Vector2D[] p = createTriangleVertices(triangleSize);


        p[0] = p[2].add(p[0].subtract(p[2]).scalarMultiply(height));
        p[1] = p[2].add(p[1].subtract(p[2]).scalarMultiply(height));


        path.moveTo((float)p[0].getX()+x, (float)p[0].getY()+y);
        path.lineTo((float)p[1].getX()+x, (float)p[1].getY()+y);
        path.lineTo((float)p[2].getX()+x, (float)p[2].getY()+y);
        path.close();

        return path;
    }

    /**
     * Function that generates a triangle path
     */
    private Path createInsideTriangle(int x, int y, float triangleSize, float percentFilled, boolean flip){
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        triangleSize /= 2;

        Vector2D[] p = createTriangleVertices(triangleSize);
        Vector2D center = new Vector2D(0,0);

        //Vector2D pos = new Vector2D(triangleSize,triangleSize);
        if(pos.distance(center) > 0) {
            //pos = pos.normalize().scalarMultiply(triangleSize);

            _pos = pos.scalarMultiply(triangleSize);

            double[] dist = new double[3];
            for (int i = 0; i < 3; i++) {
                dist[i] = _pos.distance(p[i]);
            }

            //In this case _pos is on the left side
            int opposite = 0;
            if (dist[1] > dist[0] && dist[1] > dist[2]){
                //In this case _pos is on the right

                //if _pos distance to top-left is further than top-right
                //and top-left is further than bottom
                opposite = 1;
            } else if (dist[2] > dist[0] && dist[2] > dist[1]) {
                //In this case _pos is above the triangle

                //if _pos distance to bottom is more than to top-right
                //and bottom is further than top-left
                opposite = 2;
            }

            Vector2D a = p[(opposite + 1) % 3];
            Vector2D b = p[(opposite + 2) % 3];
            Vector2D c = p[opposite];

            Line dir = new Line(new Vector2D(0, 0), _pos, 0.001);
            //never usedVector2D intersection = dir.intersection(new Line(a, b, 0.001));

            normal = _pos.normalize().scalarMultiply(-100);
            Vector2D oth = new Vector2D(-normal.getY(), normal.getX());

/*          ----
            double abSplit = intersection.distance(a) / a.distance(b);
            if(abSplit > 0.5) abSplit = intersection.distance(b) / a.distance(b);;
            abSplit *= 2.0;*/

            sideA = a.add(b.subtract(a).scalarMultiply(percentFilled));
            sideB = a.add(c.subtract(a).scalarMultiply(percentFilled));

            if(_pos.distance(b) < _pos.distance(a)){
                sideA = b.add(a.subtract(b).scalarMultiply(percentFilled));
                sideB = b.add(c.subtract(b).scalarMultiply(percentFilled));
            }

            pivot = sideA.add(sideB.subtract(sideA).scalarMultiply(_percent));

            Line othLine = new Line(pivot, pivot.add(oth), 0.001);

            Vector2D ac = othLine.intersection(new Line(a,c,0.001));
            Vector2D bc = othLine.intersection(new Line(b,c,0.001));
            Vector2D ab = othLine.intersection(new Line(a,b,0.001));

            float bcDist=0;
            if(bc != null){
                bcDist = (float) bc.distance(center);
            }
            float acDist=0;
            if(ac != null){
                acDist = (float) ac.distance(center);
            }
            float abDist=0;
            if(ab != null){
                abDist = (float) ab.distance(center);
            }

            // First case that needs a 4 vertex shape
            if(ac != null && bc != null && bcDist < triangleSize && acDist < triangleSize) {
                path.moveTo((float) a.getX() + x, (float) a.getY() + y);
                path.lineTo((float) b.getX() + x, (float) b.getY() + y);
                path.lineTo((float) bc.getX() + x, (float) bc.getY() + y);
                path.lineTo((float) ac.getX() + x, (float) ac.getY() + y);
                path.close();
            } else if(bc != null && ab != null && bcDist < triangleSize && abDist < triangleSize){
                path.moveTo((float) b.getX() + x, (float) b.getY() + y);
                path.lineTo((float) bc.getX() + x, (float) bc.getY() + y);
                path.lineTo((float) ab.getX() + x, (float) ab.getY() + y);
                path.close();
            } else if(ac != null && ab != null && acDist < triangleSize && abDist < triangleSize){
                path.moveTo((float) ac.getX() + x, (float) ac.getY() + y);
                path.lineTo((float) a.getX() + x, (float) a.getY() + y);
                path.lineTo((float) ab.getX() + x, (float) ab.getY() + y);
                path.close();
            }
        }
        return path;
    }



    public void draw(Canvas c){
        super.draw(c);

        paint.setAntiAlias(true);

        // Background
        paint.setColor(color_background);
        c.drawRect(0, 0, c.getWidth(), c.getHeight(), paint);


        int x = c.getWidth()/2;
        int y = c.getHeight()/2 - (int)(30f*pixelDensity);


        float triangleSize = (float) (c.getWidth()*0.7);

        if(connected) {
            // Outer triangle
            paint.setColor(color_triangle_background);
            c.drawPath(createTriangle(x, y, triangleSize, 1, false), paint);

            // Inner triangle
            paint.setColor(color_triangle_foreground);
            Path insidePath;
            if(_percent > 0.995){
                insidePath = createTriangle(x, y, triangleSize, _percent, false);
                vel = new Vector2D(0,0);
                pos = new Vector2D(0,1);
                _pos = new Vector2D(0,1);
            } else {
                insidePath = createInsideTriangle(x, y, triangleSize, _percent, false);
            }
            c.drawPath(insidePath, paint);
        } else {
            c.save();
            c.translate(x, y);
            c.rotate(180);
            paint.setColor(color_triangle_critical);
            c.drawPath(createTriangle(0, 0, triangleSize, 1, true), paint);
            c.restore();
        }

        // Text
/*        String labelPost = Integer.toString(Math.round(percent *100))+"%";
        if(!connected){
            labelPost = "Not connected";
        }*/

        if(DEBUG_DRAW){
            drawDebug(c, x, y);
        }

        drawText(label1, label2, x, (int) (y + triangleSize/2+10), c);
    }


    public void drawDebug(Canvas c, float x, float y) {
        c.save();
        c.translate(x, y);
        Paint pp = new Paint();
        pp.setColor(Color.RED);
        pp.setStyle(Paint.Style.FILL);
        float sz = 20f;
        c.drawRect((float)sideA.getX()-sz, (float)sideA.getY()-sz, (float)sideA.getX()+sz, (float)sideA.getY()+sz, pp);
        pp.setColor(Color.BLUE);
        c.drawRect((float)sideB.getX()-sz, (float)sideB.getY()-sz, (float)sideB.getX()+sz, (float)sideB.getY()+sz, pp);

        pp.setColor(Color.GREEN);
        c.drawRect((float)normal.getX() - sz, (float)normal.getY()-sz, (float)normal.getX() + sz, (float)normal.getY() + sz, pp);

        Vector2D oth = new Vector2D(-normal.getY(), normal.getX());
        //pp.setColor(Color.YELLOW);
        //c.drawRect((float)oth.getX()-sz, (float)oth.getY()-sz, (float)oth.getX()+sz, (float)oth.getY()+sz, pp);

        pp.setColor(Color.BLACK);
        c.drawRect((float)_pos.getX()-sz, (float)_pos.getY()-sz, (float)_pos.getX()+sz, (float)_pos.getY()+sz, pp);

        pp.setColor(Color.YELLOW);
        c.drawRect((float)pivot.getX()-sz, (float)pivot.getY()-sz, (float)pivot.getX()+sz, (float)pivot.getY()+sz, pp);

        c.restore();

    }



    /**
     * is the device currently connected to a wifi signal?
     * @return
     */
    public boolean isCritical(){
        return !connected;
    }



    public boolean shouldDraw(){
        boolean redraw = super.shouldDraw();

        if(vel == null) vel = new Vector2D(0,0);
        if(pos == null) pos = new Vector2D(0,1);
        if(_pos == null) _pos = new Vector2D(0,1);

        Vector2D a = new Vector2D(mOrientation[2]*0.01, -mOrientation[1]*0.01);
        a = a.add(pos.scalarMultiply(-0.01));

        vel = vel.scalarMultiply(0.9);
        vel = vel.add(a);
        pos = pos.add(vel);

        pos = pos.normalize();


        if(_percent != percent){
            redraw = true;
        }
        if(_connected != connected){
            _connected = connected;
            redraw = true;
        }
        if(_pos.distance(pos) > 0.001){
            _pos = pos;
            redraw = true;
        }

        if(redraw){
            _percent = (float) animateValue(_percent, percent, 0.003);
            return true;
        }

        return false;
    }
}
