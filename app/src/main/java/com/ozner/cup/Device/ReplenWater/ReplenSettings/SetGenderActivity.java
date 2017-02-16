package com.ozner.cup.Device.ReplenWater.ReplenSettings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.CommonAdapter;
import com.ozner.cup.Base.CommonViewHolder;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.R;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SetGenderActivity extends BaseActivity{
    private static final String TAG = "SetGenderActivity";
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.lv_gender)
    ListView lvGender;

    private ArrayList<String> genderList = new ArrayList<>();
    private GenderAdatper mAdapter;
    private int gender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_gender);
        ButterKnife.inject(this);
        initToolBar();
        mAdapter = new GenderAdatper(this, R.layout.list_check_item);
        genderList.add(getString(R.string.women));
        genderList.add(getString(R.string.man));
        lvGender.setAdapter(mAdapter);
        lvGender.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//        lvGender.setItemsCanFocus(false);

        lvGender.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gender = position;
//                lvGender.setItemChecked(position, true);
//                for (int i = 0; i < genderList.size(); i++) {
//                    if (i != position)
//                        lvGender.setItemChecked(i, false);
//                }
            }
        });

        mAdapter.loadData(genderList);

        //初始化
        gender = getIntent().getIntExtra(Contacts.DEV_REPLEN_GENDER, 0);
        for (int i = 0; i < genderList.size(); i++) {
            if (i == gender) {
                lvGender.setItemChecked(i, true);
            } else {
                lvGender.setItemChecked(i, false);
            }
        }
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.water_replen_meter);
        toolbar.setNavigationIcon(R.drawable.back);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, 0, 0, getString(R.string.ensure));

        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case 0:
                ensureGender();
                break;
        }
        return true;
    }

    /**
     * 确认性别
     */
    private void ensureGender() {
        Intent resIntent = new Intent();
        resIntent.putExtra(Contacts.DEV_REPLEN_GENDER, gender);
        setResult(RESULT_OK, resIntent);
        this.finish();
    }


    class GenderAdatper extends CommonAdapter<String> {

        public GenderAdatper(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
        }

        @Override
        public void convert(CommonViewHolder holder, String item, int position) {
            holder.setText(R.id.check_item, item);
        }
    }

}
