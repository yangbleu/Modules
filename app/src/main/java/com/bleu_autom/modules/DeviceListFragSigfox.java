package co.foxcat.temphawk.branch.view.device;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.foxcat.temphawk.R;
import co.foxcat.temphawk.branch.presenter.device.DeviceListP;
import co.foxcat.temphawk.branch.presenter.device.SortDeviceP;
import co.foxcat.temphawk.branch.view.deviceDetail.DeviceDetailAct;
import co.foxcat.temphawk.common.config.ApiConfig;
import co.foxcat.temphawk.common.config.DefaultConfig;
import co.foxcat.temphawk.common.model.User;
import co.foxcat.temphawk.common.model.data.device.DeviceItemDiffCallBack;
import co.foxcat.temphawk.common.model.net.ApiError;
import co.foxcat.temphawk.common.model.net.data.RPCodeImpl;
import co.foxcat.temphawk.common.model.net.data.deviceList.DeviceListByPageReqBody;
import co.foxcat.temphawk.common.model.net.data.deviceList.DeviceListReqBody;
import co.foxcat.temphawk.common.model.net.data.deviceList.DeviceListResBody;
import co.foxcat.temphawk.common.util.ImportantDeviceNotification;
import co.foxcat.temphawk.common.view.AppManager;
import co.foxcat.temphawk.common.view.BaseActivity;
import co.foxcat.temphawk.common.view.BaseFragment;
import co.foxcat.temphawk.common.view.EmptyRecyclerView;
import co.foxcat.temphawk.company.view.dialog.DeviceInfo.DeviceInfoDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DeviceListFragSigfox extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener, DeviceListP.DeviceListV , SortDeviceP.SortDeviceListV {
    public final static String TAG = "DeviceListFragSigfox";
    private SwipeRefreshLayout swipeLayout;
    private EmptyRecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private DeviceListAdapter adapter;
    private FloatingActionButton btnTop;
    private BottomSheetBehavior bottomSheet ;
    private DeviceListP deviceListP;
    private SortDeviceP sortDeviceP ;
    private BaseActivity baseActivity;

    private static final int FAB_ITEM_MIN_COUNT = 1;
    private static final int GET_MORE_ITEM_MIN_COUNT = 5;
    private int allCount = 0;
    private int maxPage = 0;
    private int currentPage = 0;
    private static final int ROW_PER_PAGE = 10;

    private String whoComes;
    private String branchId;
    private String deviceGroupId;

    private String deviceListRefresh;
    private Observable<Long> deviceListRefreshObservable;
    private Disposable deviceListRefreshDisposable;
    private List<DeviceListResBody.Data> originData = new ArrayList<>() ;

    public List<DeviceListResBody.Data> getOriginData() {
        return originData;
    }

    public void setSortDeviceP(SortDeviceP sortDeviceP) {
        this.sortDeviceP = sortDeviceP;
        sortDeviceP.setSortDeviceListV(this);
    }

    public SortDeviceP getSortDeviceP() {
        return sortDeviceP;
    }

    public static DeviceListFragSigfox newInstance(Bundle bundle) {
        DeviceListFragSigfox page = new DeviceListFragSigfox();
        page.setArguments(bundle);
        return page;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.branch_frag_device_list_sigfox ,container, false);
        swipeLayout = v.findViewById(R.id.laySwipeRefresh);
        swipeLayout.setOnRefreshListener(this);

        if(deviceListP.getUser().getRole().equals(User.ROLE_COMPANY)){
            swipeLayout.setColorSchemeResources(R.color.companyColorAccent);
        }else{
            swipeLayout.setColorSchemeResources(R.color.branchColorAccent);
        }


        recyclerView = v.findViewById(R.id.empty_recycler);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        adapter = new DeviceListAdapter(getActivity());
        initRecycler(recyclerView, linearLayoutManager, adapter);
        btnTop.show();
        btnTop.setImageDrawable(getResources().getDrawable(ImportantDeviceNotification.getInstance().getAlertIcon()));
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        startDeviceListRefresh(deviceListRefresh);
    }

    private void startDeviceListRefresh(String deviceListRefresh){
        stopDeviceListRefresh();
        if(deviceListRefresh.equals(DefaultConfig.USER_DEVICE_LIST_REFRESH_ON)){
            deviceListRefreshDisposable = deviceListRefreshObservable
                    .compose(bindToLifecycle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> getFirstDeviceList());
        }
    }

    private void stopDeviceListRefresh(){
        if(deviceListRefreshDisposable != null && !deviceListRefreshDisposable.isDisposed()){
            deviceListRefreshDisposable.dispose();
        }
    }

    @Override
    public void onStop() {
        stopDeviceListRefresh();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        deviceListP.dispose();
        baseActivity = null;
        btnTop = null;
        super.onDestroy();
    }

    private void initRecycler(EmptyRecyclerView recycler, LinearLayoutManager linearLayoutManager, final DeviceListAdapter listAdapter) {
        recycler.setLayoutManager(linearLayoutManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setDefaultEmptyView();
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if(btnTop != null){
                            if(firstVisibleItemPosition == 0){
                                bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                btnTop.show();
                            }


                            if (firstVisibleItemPosition > 5) {
                                if(adapter.getItemCount() > 5){
                                    btnTop.animate().rotation(360).setDuration(300);
                                    btnTop.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_less_black_24dp));
                                    btnTop.animate().start();
                                }else{
                                    btnTop.setImageDrawable(getResources().getDrawable(ImportantDeviceNotification.getInstance().getAlertIcon()));
                                }


                            }else{
                                if(adapter.getItemCount() > 5){
                                    btnTop.animate().rotation(0).setDuration(300);
                                    btnTop.setImageDrawable(getResources().getDrawable(ImportantDeviceNotification.getInstance().getAlertIcon()));
                                    btnTop.animate().start();
                                }else{
                                    btnTop.setImageDrawable(getResources().getDrawable(ImportantDeviceNotification.getInstance().getAlertIcon()));
                                }
                            }

                            if(lastVisibleItemPosition+2 > allCount){
                                if(adapter.getItemCount() < 5){
                                    bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                }else{
                                    bottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                                    btnTop.hide();
                                }

                            }else{
                                bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                if(firstVisibleItemPosition < 5){
                                    btnTop.show();
                                }
                            }

                            if(adapter.getItemCount() < 5){
                                bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                btnTop.show();
                            }
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        int itemCount = recyclerView.getLayoutManager().getItemCount();
                        if ((allCount > itemCount) && (itemCount - lastVisibleItemPosition) < GET_MORE_ITEM_MIN_COUNT && !swipeLayout.isRefreshing()) {
                            //getNextDeviceList();
                        }
                        break;
                }
            }
        });

        listAdapter.setOnItemClickListener((view, position) -> {
            Intent intent = new Intent(getActivity(), DeviceDetailAct.class);
            Bundle bundle = new Bundle();
            bundle.putString(DefaultConfig.WHO_COMES, whoComes);
            bundle.putString(DefaultConfig.DEVICE_GROUP_ID, deviceGroupId);
            bundle.putString(DefaultConfig.DEVICE_ID, adapter.getItem(position).getId());
            bundle.putString(DefaultConfig.DEVICE_EXPIRED ,adapter.getItem(position).getIsExpired());
            bundle.putString(DefaultConfig.DEVICE_ACTIVATE ,adapter.getItem(position).getActivate());
            bundle.putBoolean("isECSensor" ,adapter.getItem(position).getTemperatureType().equals("EC"));
            intent.putExtras(bundle);
            startActivity(intent);
        });

        listAdapter.setOnItemLongClickListener((view,position) -> {
            Intent intent = new Intent(getActivity(), DeviceInfoDialog.class);
            Bundle bundle = new Bundle();
            bundle.putString(DefaultConfig.WHO_COMES, whoComes);
            bundle.putString(DefaultConfig.DEVICE_GROUP_ID, deviceGroupId);
            bundle.putString(DefaultConfig.DEVICE_ID, adapter.getItem(position).getId());
            bundle.putString(DefaultConfig.DEVICE_EXPIRED ,adapter.getItem(position).getIsExpired());
            bundle.putString(DefaultConfig.DEVICE_ACTIVATE ,adapter.getItem(position).getActivate());
            bundle.putBoolean("isECSensor" ,adapter.getItem(position).getTemperatureType().equals("EC"));
            intent.putExtras(bundle);
            startActivity(intent);
        });

        recycler.setAdapter(listAdapter);
    }


    @Override
    protected void initData(){
        if(getArguments() != null) {
            whoComes = getArguments().getString(DefaultConfig.WHO_COMES, "");
            branchId = getArguments().getString(DefaultConfig.BRANCH_ID, "");
            deviceGroupId = getArguments().getString(DefaultConfig.DEVICE_GROUP_ID, "");
        }else {
            whoComes = "";
            branchId = "";
            deviceGroupId = "";
        }

        if(getActivity() instanceof BaseActivity) baseActivity = (BaseActivity)getActivity();
        deviceListP = new DeviceListP(baseActivity, this);
        deviceListRefresh = DefaultConfig.USER_DEVICE_LIST_REFRESH_DEFAULT;
        deviceListRefreshObservable = Observable.interval(DefaultConfig.USER_DEVICE_LIST_REFRESH_INTERVAL,
                DefaultConfig.USER_DEVICE_LIST_REFRESH_INTERVAL, TimeUnit.SECONDS);

    }

    @Override
    protected void requestData() {
        getFirstDeviceList();
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(false);
        getFirstDeviceList();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_btnTop:
                if (linearLayoutManager.findFirstVisibleItemPosition() > 5) {
                    linearLayoutManager.scrollToPositionWithOffset(0, 0);
                    btnTop.animate().rotation(0).setDuration(300);
                    btnTop.setImageDrawable(getResources().getDrawable( ImportantDeviceNotification.getInstance().getAlertIcon()));
                    bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }else{
                    ImportantDeviceNotification.getInstance().requestIgnoreBatteryOptimizations(AppManager.getAppManager().currentActivity());
                    ImportantDeviceNotification.getInstance().toggle();
                    btnTop.setImageDrawable(getResources().getDrawable( ImportantDeviceNotification.getInstance().getAlertIcon()));
                }
                break;
        }
    }

    public void clearFab(){
        if (btnTop != null) {
            btnTop.setOnClickListener(null);
        }
        btnTop = null;
    }

    public void setFab(FloatingActionButton fab) {
        btnTop = fab;
        btnTop.setOnClickListener(this);
    }
    public void setBottomSheet(BottomSheetBehavior bottomSheet){
        this.bottomSheet = bottomSheet ;
    }
    public int getFirstVisibleItemPosition(){
        return linearLayoutManager == null ? 0: linearLayoutManager.findFirstVisibleItemPosition();
    }

    private void getFirstDeviceList(){
        currentPage = 0;
        maxPage = 0;
        getNextDeviceList();
    }

    private void getNextDeviceList() {
        List<DeviceListByPageReqBody.Search> searchType = new ArrayList<>();
        if(!TextUtils.isEmpty(branchId))
            searchType.add(new DeviceListReqBody.Search(
                    DeviceListReqBody.Search.Type.BRANCH_ID,
                    branchId));
        if(!TextUtils.isEmpty(deviceGroupId))
            searchType.add(new DeviceListReqBody.Search(
                    DeviceListReqBody.Search.Type.DEVICE_GROUP_ID,
                    deviceGroupId));
        searchType.add(new DeviceListReqBody.Search(
                DeviceListReqBody.Search.Type.CONNECT_TYPE,
                DeviceListReqBody.Search.CONNECT_TYPE_SIGFOX));
        searchType.add(new DeviceListReqBody.Search(
                DeviceListReqBody.Search.Type.STOCK,
                DeviceListReqBody.Search.STOCK_EXCLUDE));
        //deviceListP.getDeviceListPage((currentPage + 1) * ROW_PER_PAGE, searchType, true);
        deviceListP.getDeviceListAll(searchType);
    }

    @Override
    public void onApiStart() {
        baseActivity.showSimpleProgressDialog(ApiConfig.PROGRESS_TIMEOUT);
    }

    @Override
    public void onApiError(Throwable t) {
        baseActivity.dismissSimpleProgressDialog();
        ApiError.onError(getActivity(), t);
    }

    @Override
    public void onGetNowDeviceListSuccess(int allCount, String deviceGroupName, List<DeviceListResBody.Data> data, String deviceListRefresh) {

    }

    @Override
    public void onGetNowDeviceListFailure(String rpCode) {

    }

    @Override
    public void onGetNextDeviceListSuccess(int allCount, String deviceGroupName, List<DeviceListResBody.Data> data, String deviceListRefresh) {
        currentPage++;
        maxPage = (int) Math.ceil(allCount / 10f);
        if(currentPage >= maxPage) currentPage = maxPage;
        this.allCount = allCount;
        if(baseActivity.getSupportActionBar() != null){
            if(TextUtils.isEmpty(deviceGroupName)){
                baseActivity.getSupportActionBar().setTitle(R.string.devices);
            }else {
                baseActivity.getSupportActionBar().setTitle(deviceGroupName);
            }
        }
        originData.clear();
        originData.addAll(data);
        if(allCount == 0) recyclerView.setEmptyText(R.string.no_device_has_been_established);
        Observable.fromCallable(() -> DiffUtil.calculateDiff(new DeviceItemDiffCallBack(adapter.getItems(), data), true))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindToLifecycle())
                .subscribe(diffResult -> {
                    adapter.setItems(data);
                    diffResult.dispatchUpdatesTo(adapter);

                    baseActivity.dismissSimpleProgressDialog();
                    if (sortDeviceP != null) {
                        sortDeviceP.sortWithState(data);
                    }


                });
        adapter.notifyDataSetChanged();
        this.deviceListRefresh = deviceListRefresh;
        startDeviceListRefresh(deviceListRefresh);

    }

    @Override
    public void onGetNextDeviceListFailure(String rpCode) {
        baseActivity.dismissSimpleProgressDialog();
        new RPCodeImpl().showAction(getActivity(), rpCode);
    }


    @Override
    public void onDeviceListSorted(List<DeviceListResBody.Data> data) {
        adapter.setItems(data);
        adapter.notifyDataSetChanged();
    }
}
