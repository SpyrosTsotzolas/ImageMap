package com.example.imagemap;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Xml;

import androidx.annotation.RequiresApi;

import org.xmlpull.v1.XmlPullParser;

public class MainActivity extends Activity {
    ImageMap mImageMap;
//    //private PackageManager resources;
//    XmlPullParser parser = resources.getXml(myResource);
//    AttributeSet attrs = Xml.asAttributeSet(parser);

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find the image map in the view
        mImageMap = (ImageMap)findViewById(R.id.map);
        mImageMap.setImageResource(R.drawable.floor_plan);

//        // add a click handler to react when areas are tapped
//        mImageMap.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler()
//        {
//            @Override
//            public void onImageMapClicked(int id, ImageMap imageMap)
//            {
//                // when the area is tapped, show the name in a
//                // text bubble
//                mImageMap.showBubble(id);
//            }
//
//            @Override
//            public void onBubbleClicked(int id)
//            {
//                // react to info bubble for area being tapped
//            }
//        });
    }
}