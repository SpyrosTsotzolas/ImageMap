
package com.example.imagemap;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.SyncFailedException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageMap extends androidx.appcompat.widget.AppCompatImageView {

    // mMaxSize controls the maximum zoom size as a multiplier of the initial size.
    // Allowing size to go too large may result in memory problems.
    //  set this to 1.0f to disable resizing
    // by default, this is 1.5f
    private static final float defaultMaxSize = 1.5f;


    /*
     * containers for the image map areas
     * using SparseArray<Area> instead of HashMap for the sake of performance
     */
    ArrayList<Area> mAreaList = new ArrayList<Area>();
    SparseArray<Area> mIdToArea = new SparseArray<Area>();

    // click handler list
//    ArrayList<OnImageMapClickedHandler> mCallbackList;
//
//    // list of open info bubbles
//    SparseArray<Bubble> mBubbleMap = new SparseArray<Bubble>();

    // changed this from local variable to class field
    protected String mapName;


    /*
     * Constructors
     */
    public ImageMap(Context context) {
        super(context);
    }

    public ImageMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(attrs);
    }

    public ImageMap(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        loadAttributes(attrs);
    }

    /**
     * get the map name from the attributes and load areas from xml
     *
     * @param attrs
     */
    public void loadAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ImageMap);


        this.mapName = a.getString(R.styleable.ImageMap_map);
        if (mapName != null) {
            loadMap(mapName);
        }
    }

    /**
     * parse the maps.xml resource and pull out the areas
     *
     * @param map - the name of the map to load
     */
    public void loadMap(String map) {
        boolean loading = false;
        try {
            XmlResourceParser xpp = getResources().getXml(R.xml.map);

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    // Start document
                    //  This is a useful branch for a debug log if
                    //  parsing is not working
                } else if (eventType == XmlPullParser.START_TAG) {
                    String tag = xpp.getName();
                    if (tag.equalsIgnoreCase("map")) {
                        String mapname = xpp.getAttributeValue(null, "name");
                        if (mapname != null) {
                            if (mapname.equalsIgnoreCase(map)) {
                                loading = true;
                            }
                        }
                    }
                    if (loading) {
                        if (tag.equalsIgnoreCase("area")) {
                            Area a = null;
                            String shape = xpp.getAttributeValue(null, "shape");
                            String coords = xpp.getAttributeValue(null, "coords");
                            String id = xpp.getAttributeValue(null, "id");

                            // as a name for this area, try to find any of these
                            // attributes
                            //  name attribute is custom to this impl (not standard in html area tag)
                            String name = xpp.getAttributeValue(null, "name");
                            if (name == null) {
                                name = xpp.getAttributeValue(null, "title");
                            }
                            if (name == null) {
                                name = xpp.getAttributeValue(null, "alt");
                            }

                            if ((shape != null) && (coords != null)) {
                                a = addShape(shape, name, coords, id);
                                if (a != null) {
                                    // add all of the area tag attributes
                                    // so that they are available to the
                                    // implementation if needed (see getAreaAttribute)
                                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                        String attrName = xpp.getAttributeName(i);
                                        Log.d("attr", attrName);
                                        String attrVal = xpp.getAttributeValue(null, attrName);
                                        Log.d("val", attrVal);
                                        a.addValue(attrName, attrVal);
                                    }
                                }
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String tag = xpp.getName();
                    if (tag.equalsIgnoreCase("map")) {
                        loading = false;
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException xppe) {
            // Having trouble loading? Log this exception
        } catch (IOException ioe) {
            // Having trouble loading? Log this exception
        }
    }

    /**
     * Create a new area and add to tracking
     * Changed this from private to protected!
     *
     * @param shape
     * @param name
     * @param coords
     * @param id
     * @return
     */
    protected Area addShape(String shape, String name, String coords, String id) {
        Log.d("shape", "mphke");
        Area a = null;
        String rid = id.replace("@+id/", "");
        int _id = 0;

        try {
            Class<R.id> res = R.id.class;
            Field field = res.getField(rid);
            _id = field.getInt(null);
        } catch (Exception e) {
            _id = 0;
        }
        if (_id != 0) {

            if (shape.equalsIgnoreCase("poly")) {
                a = new PolyArea(_id, name, coords);
            }
            if (a != null) {
                addArea(a);
            }
        }
        return a;
    }

    public void addArea(Area a) {
        mAreaList.add(a);
        mIdToArea.put(a.getId(), a);
    }


    public String getAreaAttribute(int areaId, String key) {
        String value = null;
        Area a = mIdToArea.get(areaId);
        if (a != null) {
            value = a.getValue(key);
        }
        return value;
    }


    /*
     * Begin map area support
     */

    /**
     * Area is abstract Base for tappable map areas
     * descendants provide hit test and focal point
     */
    abstract class Area {
        int _id;
        String _name;
        HashMap<String, String> _values;

        public Area(int id, String name) {
            _id = id;
            if (name != null) {
                _name = name;
            }
        }

        public int getId() {
            return _id;
        }

        public String getName() {
            return _name;
        }

        // all xml values for the area are passed to the object
        // the default impl just puts them into a hashmap for
        // retrieval later
        public void addValue(String key, String value) {
            if (_values == null) {
                _values = new HashMap<String, String>();
            }
            _values.put(key, value);
        }

        public String getValue(String key) {
            String value = null;
            if (_values != null) {
                value = _values.get(key);
            }
            return value;
        }

    }

    /**
     * Polygon area
     */
    class PolyArea extends Area {
        ArrayList<Integer> xpoints = new ArrayList<Integer>();
        ArrayList<Integer> ypoints = new ArrayList<Integer>();

        // centroid point for this poly
        float _x;
        float _y;

        // number of points (don't rely on array size)
        int _points;

        // bounding box
        int top = -1;
        int bottom = -1;
        int left = -1;
        int right = -1;

        public PolyArea(int id, String name, String coords) {
            super(id, name);

            // split the list of coordinates into points of the
            // polygon and compute a bounding box
            String[] v = coords.split(",");

            int i = 0;
            while ((i + 1) < v.length) {
                int x = Integer.parseInt(v[i]);
                int y = Integer.parseInt(v[i + 1]);
                xpoints.add(x);
                ypoints.add(y);
                top = (top == -1) ? y : Math.min(top, y);
                bottom = (bottom == -1) ? y : Math.max(bottom, y);
                left = (left == -1) ? x : Math.min(left, x);
                right = (right == -1) ? x : Math.max(right, x);
                i += 2;
            }
            _points = xpoints.size();

            // add point zero to the end to make
            // computing area and centroid easier
            xpoints.add(xpoints.get(0));
            ypoints.add(ypoints.get(0));

            computeCentroid();
        }

        /**
         * area() and computeCentroid() are adapted from the implementation
         * of polygon.java  published from a princeton case study
         * The study is here: http://introcs.cs.princeton.edu/java/35purple/
         * The polygon.java source is here: http://introcs.cs.princeton.edu/java/35purple/Polygon.java.html
         */

        // return area of polygon
        public double area() {
            double sum = 0.0;
            for (int i = 0; i < _points; i++) {
                sum = sum + (xpoints.get(i) * ypoints.get(i + 1)) - (ypoints.get(i) * xpoints.get(i + 1));
            }
            sum = 0.5 * sum;
            return Math.abs(sum);
        }

        // compute the centroid of the polygon
        public void computeCentroid() {
            double cx = 0.0, cy = 0.0;
            for (int i = 0; i < _points; i++) {
                cx = cx + (xpoints.get(i) + xpoints.get(i + 1)) * (ypoints.get(i) * xpoints.get(i + 1) - xpoints.get(i) * ypoints.get(i + 1));
                cy = cy + (ypoints.get(i) + ypoints.get(i + 1)) * (ypoints.get(i) * xpoints.get(i + 1) - xpoints.get(i) * ypoints.get(i + 1));
            }
            cx /= (6 * area());
            cy /= (6 * area());
            _x = Math.abs((int) cx);
            _y = Math.abs((int) cy);
        }
    }
    // Log.d("shape", shape);
}
