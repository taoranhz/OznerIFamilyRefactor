package com.ozner.cup.MyCenter.MyFriend;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ozner.cup.Base.BaseActivity;
import com.ozner.cup.Base.CommonAdapter;
import com.ozner.cup.Base.CommonViewHolder;
import com.ozner.cup.Bean.Contacts;
import com.ozner.cup.Command.OznerPreference;
import com.ozner.cup.Command.UserDataPreference;
import com.ozner.cup.DBHelper.DBManager;
import com.ozner.cup.DBHelper.UserInfo;
import com.ozner.cup.HttpHelper.HttpMethods;
import com.ozner.cup.HttpHelper.OznerHttpResult;
import com.ozner.cup.HttpHelper.ProgressSubscriber;
import com.ozner.cup.R;
import com.ozner.cup.Utils.LCLogUtils;
import com.ozner.cup.Utils.MobileInfoUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AddFriendActivity extends BaseActivity implements TextView.OnEditorActionListener {
    private static final String TAG = "AddFriendActivity";
    private final int LOCAL_REQUEST_CODE = 1;
    private final int SEARCH_REQUEST_CODE = 2;
    @InjectView(R.id.title)
    TextView title;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.et_searchValue)
    EditText etSearchValue;
    @InjectView(R.id.llay_btn_search)
    LinearLayout llayBtnSearch;
    @InjectView(R.id.iv_headImg)
    ImageView ivHeadImg;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_btn_add)
    TextView tvBtnAdd;
    @InjectView(R.id.llay_searchResult)
    LinearLayout llaySearchResult;
    @InjectView(R.id.lv_contacts)
    ListView lvContacts;
    @InjectView(R.id.pb_loading)
    ProgressBar pbLoading;
    @InjectView(R.id.tv_errMsg)
    TextView tvErrMsg;
    @InjectView(R.id.llay_loading)
    LinearLayout llayLoading;
    @InjectView(R.id.tv_search_result)
    TextView tvSearchResult;
    @InjectView(R.id.llay_searchContent)
    LinearLayout llaySearchContent;

    private PermissionUtil.PermissionRequestObject perReqResult;
    private List<UserInfo> contactList;
    private ContactAdatper mAdapter;
    private String mMobile;
    private String mUserid;
    private UserInfo searchInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ButterKnife.inject(this);
        contactList = new ArrayList<>();
        mAdapter = new ContactAdatper(this, R.layout.contact_msg_item);
        lvContacts.setAdapter(mAdapter);
        try {
            mUserid = UserDataPreference.GetUserData(this, UserDataPreference.UserId, "");
            mMobile = DBManager.getInstance(this).getUserInfo(mUserid).getMobile();
        } catch (Exception ex) {
            LCLogUtils.E(TAG, "onCreate_Ex:" + ex.getMessage());
        }
        etSearchValue.setOnEditorActionListener(AddFriendActivity.this);
        initToolBar();
        checkLocalPhone();
//        initTestData();
//        loadSearchInfo(searchInfo);
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        title.setText(R.string.add_friend);
        toolbar.setBackgroundColor(Color.WHITE);
        title.setTextColor(Color.BLACK);
        toolbar.setNavigationIcon(R.drawable.back);
    }

//    private void initTestData() {
//
//        for (int i = 0; i < 10; i++) {
//            UserInfo info = new UserInfo();
//            info.setStatus(0);
//            info.setHeadimg("http://image.baidu.com/search/detail?ct=503316480&z=&tn=baiduimagedetail&ipn=d&word=%E5%B0%8F%E6%B8%85%E6%96%B0&step_word=&ie=utf-8&in=&cl=2&lm=-1&st=-1&cs=770718883,1054717470&os=4059680718,655695298&simid=0,0&pn=12&rn=1&di=15958031210&ln=1948&fr=&fmq=1459502282690_R&fm=&ic=0&s=undefined&se=&sme=&tab=0&width=&height=&face=undefined&is=0,0&istype=2&ist=&jit=&bdtype=0&spn=0&pi=0&gsm=0&objurl=http%3A%2F%2Fimg3.fengniao.com%2Fforum%2Fattachpics%2F765%2F112%2F30582220_600.jpg&rpstart=0&rpnum=0&adpicid=0");
//            info.setNickname("小四 " + i);
//            info.setMobile("1523619373" + i);
//            if (i == 2) {
//                searchInfo = info;
//            }
//            contactList.add(info);
//        }
//        lvContacts.setVisibility(View.VISIBLE);
//        llayLoading.setVisibility(View.GONE);
//        mAdapter.loadData(contactList);
//    }

    /**
     * 显示搜索结果
     */
    private void showSearchReuslt() {
        llaySearchResult.setVisibility(View.VISIBLE);
        llaySearchContent.setVisibility(View.VISIBLE);
        tvSearchResult.setVisibility(View.GONE);
    }

    /**
     * 显示没有结果
     */
    private void showNoSearchResult() {
        llaySearchResult.setVisibility(View.VISIBLE);
        llaySearchContent.setVisibility(View.GONE);
        tvSearchResult.setVisibility(View.VISIBLE);
        tvSearchResult.setText(R.string.friend_search_fail);
    }

    /**
     * 显示没有通讯录好友
     */
    private void showNoContactFriend() {
        llayLoading.setVisibility(View.VISIBLE);
        pbLoading.setVisibility(View.GONE);
        tvErrMsg.setText(R.string.no_contacts_friend);
    }

    /**
     * 显示加载通讯录好友
     */
    private void showLoadingContactFriend() {
        llayLoading.setVisibility(View.VISIBLE);
        pbLoading.setVisibility(View.VISIBLE);
        tvErrMsg.setText(R.string.loading);
    }

    /**
     * 校验本地通讯录
     */
    private void checkLocalPhone() {
        showLoadingContactFriend();
        perReqResult = PermissionUtil.with(this).request(Manifest.permission.READ_CONTACTS)
                .onAllGranted(new Func() {
                    @Override
                    protected void call() {
                        String phoneStr = MobileInfoUtil.getLocalPhoneNumbers(AddFriendActivity.this);
                        if (phoneStr != null) {
                            loadContactFriend(phoneStr);
                        } else {
                            showNoContactFriend();
                        }
                    }
                }).onAnyDenied(new Func() {
                    @Override
                    protected void call() {
                        showToastCenter(R.string.user_deny_red_contacts);
                        showNoContactFriend();
                    }
                }).ask(1);
    }

    @OnClick({R.id.llay_btn_search, R.id.tv_btn_add})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llay_btn_search:
                search();
                break;
            case R.id.tv_btn_add:
                Intent verifyIntent = new Intent(this, SendVerifyActivity.class);
                verifyIntent.putExtra(Contacts.PARMS_CLICK_POS, -1);
                verifyIntent.putExtra(Contacts.PARMS_PHONE, searchInfo.getMobile());
                if (verifyIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(verifyIntent, SEARCH_REQUEST_CODE);
                }
                break;
        }
    }

    /**
     * 开始查询
     */
    private void search() {
        String mobile = etSearchValue.getText().toString().trim();
        if (mobile.length() == 11) {
            if (!mobile.equals(mMobile)) {
                searchFriend(etSearchValue.getText().toString().trim());
            } else {
                showToastCenter(R.string.input_friend_phone);
            }
        } else {
            showToastCenter(R.string.input_right_phone);
        }
    }

    /**
     * 查找好友
     *
     * @param mobile
     */
    private void searchFriend(String mobile) {
        searchInfo = null;
        HttpMethods.getInstance().getUserNickImage(OznerPreference.getUserToken(this),
                mobile, new ProgressSubscriber<JsonObject>(this, getString(R.string.searching), false, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        showNoSearchResult();
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        if (jsonObject != null) {
                            if (jsonObject.get("state").getAsInt() > 0) {
                                JsonArray array = jsonObject.getAsJsonArray("data");
                                if (!array.isJsonNull() && array.size() > 0) {
                                    List<UserInfo> result = new Gson().fromJson(array, new TypeToken<List<UserInfo>>() {
                                    }.getType());
                                    searchInfo = result.get(0);
                                    if (searchInfo != null) {
                                        loadSearchInfo(searchInfo);
                                    } else {
                                        showNoSearchResult();
                                    }
                                } else {
                                    if (jsonObject.get("state").getAsInt() == -10006
                                            || jsonObject.get("state").getAsInt() == -10007) {
                                        BaseActivity.reLogin(AddFriendActivity.this);
                                    } else {
                                        showNoSearchResult();
                                    }
                                }
                            } else {
                                showNoSearchResult();
                            }
                        } else {
                            showNoSearchResult();
                        }
                    }
                }));
    }

    /**
     * 加载搜索到的好友信息
     *
     * @param info
     */
    private void loadSearchInfo(UserInfo info) {
        if (info.getStatus() != -10013) {
            showSearchReuslt();
            if (info.getNickname() != null && !info.getNickname().isEmpty()) {
                tvName.setText(info.getNickname());
            } else {
                tvName.setText(info.getMobile());
            }

            switch (info.getStatus()) {
                case 0:
                    tvBtnAdd.setEnabled(true);
                    tvBtnAdd.setText(R.string.append);
                    break;
                case 1://等待验证
                    tvBtnAdd.setEnabled(false);
                    tvBtnAdd.setText(R.string.wait_verify);
                    break;
                case 2:
                    tvBtnAdd.setEnabled(false);
                    tvBtnAdd.setText(R.string.added);
                    break;
            }
            Glide.with(this)
                    .load(info.getHeadimg())
                    .asBitmap()
                    .placeholder(R.drawable.icon_default_headimage)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(ivHeadImg) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            ivHeadImg.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else {
            showNoSearchResult();
            tvSearchResult.setText(R.string.is_not_ozner_user);
        }
    }

    /**
     * 加载通讯录好友
     */
    private void loadContactFriend(final String jsonMobile) {
        HttpMethods.getInstance().getUserNickImage(OznerPreference.getUserToken(this),
                jsonMobile,
                new ProgressSubscriber<JsonObject>(this, new OznerHttpResult<JsonObject>() {
                    @Override
                    public void onError(Throwable e) {
                        showNoContactFriend();
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        if (jsonObject != null) {
                            if (jsonObject.get("state").getAsInt() > 0) {
                                lvContacts.setVisibility(View.VISIBLE);
                                llayLoading.setVisibility(View.GONE);
                                JsonArray array = jsonObject.getAsJsonArray("data");
                                contactList.clear();
                                List<UserInfo> result = new Gson().fromJson(array, new TypeToken<List<UserInfo>>() {
                                }.getType());
                                for (UserInfo info : result) {
                                    if (info.getStatus() >= 0 && !info.getMobile().equals(mMobile)) {
                                        contactList.add(info);
                                    }
                                }
                                if (contactList.size() > 0) {
                                    mAdapter.loadData(contactList);
                                } else {
                                    showNoContactFriend();
                                }
                            } else {
                                if (jsonObject.get("state").getAsInt() == -10006
                                        || jsonObject.get("state").getAsInt() == -10007) {
                                    BaseActivity.reLogin(AddFriendActivity.this);
                                } else {
                                    showNoContactFriend();
                                }
                            }
                        } else {
                            showNoContactFriend();
                        }
                    }
                }));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (perReqResult != null) {
            perReqResult.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case LOCAL_REQUEST_CODE:
                    int clickPos = data.getIntExtra(Contacts.PARMS_CLICK_POS, -1);
                    String mobile = data.getStringExtra(Contacts.PARMS_PHONE);

                    if (clickPos >= 0) {
                        contactList.get(clickPos).setStatus(1);
                        mAdapter.loadData(contactList);
                    }
                    if (searchInfo.getMobile().equals(mobile)) {
                        tvBtnAdd.setText(R.string.wait_verify);
                        tvBtnAdd.setEnabled(false);
                    }
                    break;
                case SEARCH_REQUEST_CODE:
                    String search_mobile = data.getStringExtra(Contacts.PARMS_PHONE);
                    tvBtnAdd.setText(R.string.wait_verify);
                    tvBtnAdd.setEnabled(false);
                    int count = contactList.size();
                    for (int i = 0; i < count; i++) {
                        if (contactList.get(i).getMobile().equals(search_mobile)) {
                            contactList.get(i).setStatus(1);
                            break;
                        }
                    }
                    mAdapter.loadData(contactList);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            search();
        }
        return true;
    }

    class ContactAdatper extends CommonAdapter<UserInfo> {

        public ContactAdatper(Context context, int itemLayoutId) {
            super(context, itemLayoutId);
        }

        @Override
        public void convert(CommonViewHolder holder, final UserInfo item, final int position) {
            final ImageView ivHeadImg = holder.getView(R.id.iv_headImg);
            Glide.with(mContext)
                    .load(item.getHeadimg())
                    .asBitmap()
                    .placeholder(R.drawable.icon_default_headimage)
                    .centerCrop()
                    .into(new BitmapImageViewTarget(ivHeadImg) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            ivHeadImg.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            if (item.getNickname() != null && !item.getNickname().isEmpty()) {
                holder.setText(R.id.tv_name, item.getNickname());
            } else {
                holder.setText(R.id.tv_name, item.getMobile());
            }

            switch (item.getStatus()) {
                case 0://未添加
                    holder.setText(R.id.tv_btn_add, R.string.append);
                    holder.getView(R.id.tv_btn_add).setEnabled(true);
                    holder.getView(R.id.tv_btn_add).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            Toast.makeText(mContext, "添加：" + position, Toast.LENGTH_SHORT).show();
                            Intent verifyIntent = new Intent(AddFriendActivity.this, SendVerifyActivity.class);
                            verifyIntent.putExtra(Contacts.PARMS_CLICK_POS, position);
                            verifyIntent.putExtra(Contacts.PARMS_PHONE, item.getMobile());
                            if (verifyIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(verifyIntent, LOCAL_REQUEST_CODE);
                            }
                        }
                    });
                    break;
                case 1://等待验证
                    holder.setText(R.id.tv_btn_add, R.string.wait_verify);
                    holder.getView(R.id.tv_btn_add).setEnabled(false);
                    break;
                case 2://已添加
                    holder.setText(R.id.tv_btn_add, R.string.added);
                    holder.getView(R.id.tv_btn_add).setEnabled(false);
                    break;
            }
        }
    }

}
