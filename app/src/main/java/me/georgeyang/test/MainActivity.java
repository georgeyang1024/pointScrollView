package me.georgeyang.test;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import me.georgeyang.pointscrollview.PointScrollView;
import me.georgeyang.test.entity.PlayGroundPoint;
import me.georgeyang.test.util.FileUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        PointScrollView mapview = (PointScrollView) findViewById(R.id.mapview);
        try {
            String json = FileUtil.readAssertResource(this,"amenities.json");
            JSONTokener jsonParser = new JSONTokener(json);
            JSONObject jsonObject = (JSONObject) jsonParser.nextValue();
            JSONArray amenities = jsonObject.getJSONArray("amenities");
            for (int i=0;i<amenities.length();i++) {
                JSONObject object = amenities.getJSONObject(i);
                PlayGroundPoint point = new PlayGroundPoint();
                point.x = object.getInt("x");
                point.y = object.getInt("y");
                point.itemid = object.getInt("itemid");
                point.category = object.getInt("category");
                point.name = object.getString("name");
                point.icon = object.getString("icon");
                point.image = object.getString("image");
                point.description = object.getString("description");
                mapview.drawPoint(point.x,point.y,point.getDrawableRes(this),point);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapview.setOnPointClickListener(new PointScrollView.OnPointClickListener() {
            @Override
            public void onCick(View pointView, Object point) {
                PlayGroundPoint clickPoint = (PlayGroundPoint)point;
                Toast.makeText(getApplicationContext(),"clickPoint:" + clickPoint.description,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
