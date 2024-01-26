package com.zonetech.online.payment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.payu.base.models.ErrorResponse;
import com.payu.base.models.PayUPaymentParams;
import com.payu.base.models.PaymentMode;
import com.payu.base.models.PaymentType;
import com.payu.checkoutpro.PayUCheckoutPro;
import com.payu.checkoutpro.models.PayUCheckoutProConfig;
import com.payu.checkoutpro.utils.PayUCheckoutProConstants;
import com.payu.ui.model.listeners.PayUCheckoutProListener;
import com.payu.ui.model.listeners.PayUHashGenerationListener;
import com.zonetech.online.BaseApplication;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.mypackage.onlineTestSeries.TestUtils;
import com.zonetech.online.offers.OfferDialogActivity;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CartActivity extends ZTAppCompatActivity implements CartAdapter.ClickListener, View.OnClickListener {
    public static final int PAYMENT_START = 1;
    public static final int PAYMENT_PAYMENT_GATEWAY = 2;
    public static final int PAYMENT_RETURN_BACK = 3;
    public static final int PAYMENT_DECLINE = 4;
    public static final int PAYMENT_FAILED = 5;
    public static final int PAYMENT_SUCCESS = 6;

    public static final double GST = 18;
    public static final String TAG = "CartActivity";
    public static final String EXTRA_PACKAGE_ID = "packageId";
    public static final String EXTRA_SUBJECT_ID = "subjectId";
    public static final String EXTRA_SCREEN_TYPE = "type";
    public static final String EXTRA_SCREEN_DURATION = "duration";
    public static final String EXTRA_SCREEN_MRP = "mrp";
    public static final String EXTRA_SCREEN_FEES = "fees";
    private RecyclerView cartList;
    private TextView totalAmountView;
    private TextView discountView;
    private TextView amountView;
    private TextView gstView;
    private TextView payAmountView;
    private double amount;
    private double totalamount;
    private long payAmount;
    private double discount;
    private JSONArray items;
    private String currency;
    private CartAdapter cartAdapter;
    private int packageId;
    private int subjectId;
    private String packageName = "";
    private String transId;
    private String promoCodeValue = "";
    private double originalAmount;
    private double originalTotalamount;
    private long originalPayAmount;
    private long gstAmount;
    private String type = "other";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        cartList = findViewById(R.id.cartList);
        totalAmountView = findViewById(R.id.totalAmount);
        amountView = findViewById(R.id.amount);
        gstView = findViewById(R.id.gst);
        payAmountView = findViewById(R.id.payAmount);
        discountView = findViewById(R.id.discount);
        currency = getString(R.string.currency);
        findViewById(R.id.payButton).setOnClickListener(this);
        findViewById(R.id.progressBar).setOnClickListener(this);
        findViewById(R.id.removeCode).setOnClickListener(this);
        findViewById(R.id.applyPromoCode).setOnClickListener(this);
        findViewById(R.id.offer).setOnClickListener(this);
        ((EditText)findViewById(R.id.promoCode)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    applyPromoCode();
                    return true; // consume.
                }
                return false; // pass on to other listeners.
            }
        });
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            packageId = bundle.getInt(EXTRA_PACKAGE_ID);
            subjectId = bundle.getInt(EXTRA_SUBJECT_ID);
            type = bundle.getString(EXTRA_SCREEN_TYPE, type);
            JSONObject params = new JSONObject();
            try{
                params.put("StudentID", Utils.getStudentId(this));
                params.put("PackageID", packageId);
                params.put("SubjectID", subjectId);
                if(bundle.containsKey(EXTRA_SCREEN_DURATION)){
                    params.put("Duration", bundle.getInt(EXTRA_SCREEN_DURATION));
                    params.put("MRP", bundle.getInt(EXTRA_SCREEN_MRP));
                    params.put("Fees", bundle.getInt(EXTRA_SCREEN_FEES));
                }
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
            addToCart(params);
        }else{
            getCart();
        }
    }

    private void addToCart(JSONObject params){
        PaymentUtils.addToCart(this, params, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(CartActivity.this)){
                    return;
                }
                getCart();
            }

            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCart(){
        if(findViewById(R.id.appliedLayout).getVisibility() == View.VISIBLE){
            showPromoCode();
        }
        PaymentUtils.getCart(this, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(CartActivity.this)){
                    return;
                }
                JSONObject data = response.optJSONObject("Body");
                items = data.optJSONArray("Items");
                if(items != null && items.length() > 0) {
                    totalamount = data.optDouble("TotalAmont");
                    for (int i = 0 ; i < items.length() ; i++){
                        packageName = packageName + items.optJSONObject(i).optInt("PackageID");
                    }
                    Preferences.put(getApplicationContext(), Preferences.KEY_CART_COUNT, items.length());
                    setPriceView();
                    originalAmount = amount;
                    originalPayAmount = payAmount;
                    originalTotalamount = totalamount;
                    setCartList();
                    //getPromoCoupon();
                }else{
                    findViewById(R.id.noItemLayout).setVisibility(View.VISIBLE);
                    Preferences.put(getApplicationContext(), Preferences.KEY_CART_COUNT, 0);
                }
            }

            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    private void setCartList(){
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        if(cartAdapter != null){
            cartAdapter.refreshValues(items);
        }else{
            cartAdapter = new CartAdapter(this, items, this);
            cartList.setLayoutManager(new LinearLayoutManager(this));
            cartList.setAdapter(cartAdapter);
        }
        findViewById(R.id.footer).setVisibility(View.VISIBLE);
        findViewById(R.id.footerShadow).setVisibility(View.VISIBLE);
    }

    public void removeCart(int cartId){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        PaymentUtils.removeCart(this, cartId, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(CartActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                getCart();
            }

            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    private void setPriceView(){
        totalAmountView.setText(String.format(getString(R.string.total_amount), currency+Math.round(totalamount)));
        double perDiscount = discount/totalamount;
        discountView.setText(String.format(getString(R.string.discount), currency+Math.round(discount), Math.round(perDiscount*100)+"%"));
        amount = totalamount - discount;
        amountView.setText(String.format(getString(R.string.amount), currency+Math.round(amount)));
//        double gst = (amount * GST)/100;
        gstAmount = 0;//Math.round(gst);
//        gstView.setText(String.format(getString(R.string.gst), GST+"%", currency+gstAmount));
        payAmount = Math.round(amount) + gstAmount;
        payAmountView.setText(String.format(getString(R.string.pay_amount), currency+payAmount));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.offer:
                offer();
                break;
            case R.id.applyPromoCode:
                applyPromoCode();
                break;
            case R.id.removeCode:
                showPromoCode();
                break;
            case R.id.progressBar:
                return;
            case R.id.payButton:
                payButtonClicked();
                break;
        }
    }
    private void payButtonClicked(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        PaymentUtils.buyPackage(this, Math.round(payAmount), Math.round(discount), promoCodeValue, gstAmount, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(CartActivity.this)){
                    return;
                }
                response = response.optJSONObject("Body");
                transId = response.optString("Txnid");
                if(payAmount == 0){
                    sendPaymentStatus(PAYMENT_SUCCESS);
                }else {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    initUiSdk(preparePayUBizParams());
                }
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initUiSdk(PayUPaymentParams payUPaymentParams) {
        PayUCheckoutPro.open(
                this,
                payUPaymentParams,
                getCheckoutProConfig(),
                new PayUCheckoutProListener() {

                    @Override
                    public void onPaymentSuccess(Object response) {
                        sendPaymentStatus(PAYMENT_RETURN_BACK);
                        updatePaymentStatus(response);
                    }

                    @Override
                    public void onPaymentFailure(Object response) {
                        sendPaymentStatus(PAYMENT_RETURN_BACK);
                        updatePaymentStatus(response);
                    }

                    @Override
                    public void onPaymentCancel(boolean isTxnInitiated) {
                        Utils.showToast(CartActivity.this, "Transaction cancelled by user");
                        sendPaymentStatus(PAYMENT_RETURN_BACK);
                    }

                    @Override
                    public void onError(ErrorResponse errorResponse) {
                        sendPaymentStatus(PAYMENT_RETURN_BACK);
                        String errorMessage = errorResponse.getErrorMessage();
                        if (TextUtils.isEmpty(errorMessage))
                            errorMessage = "Some error occurred";
                        Utils.showToast(CartActivity.this, errorMessage);
                    }

                    @Override
                    public void setWebViewProperties(@Nullable WebView webView, @Nullable Object o) {
                        //For setting webview properties, if any. Check Customized Integration section for more details on this
                    }

                    @Override
                    public void generateHash(HashMap<String, String> valueMap, PayUHashGenerationListener hashGenerationListener) {
                        AppEnvironment appEnvironment = ((BaseApplication) getApplication()).getAppEnvironment();
                        String hashName = valueMap.get(PayUCheckoutProConstants.CP_HASH_NAME);
                        String hashData = valueMap.get(PayUCheckoutProConstants.CP_HASH_STRING);
                        if (!TextUtils.isEmpty(hashName) && !TextUtils.isEmpty(hashData)) {
                            String salt = appEnvironment.salt();
                            if (valueMap.containsKey(PayUCheckoutProConstants.CP_POST_SALT))
                                salt = salt + "" + (valueMap.get(PayUCheckoutProConstants.CP_POST_SALT));
                            String hash = null;
                            if (hashName.equalsIgnoreCase(PayUCheckoutProConstants.CP_LOOKUP_API_HASH)) {
                                hash = calculateHmacSHA1Hash(hashData, appEnvironment.merchant_Key());
                            } else {
                                hash = calculateHash(hashData + salt);
                            }
                            HashMap<String, String> dataMap = new HashMap<>();
                            dataMap.put(hashName, hash);
                            hashGenerationListener.onHashGenerated(dataMap);
                        }
                    }
                }
        );
        sendPaymentStatus(PAYMENT_PAYMENT_GATEWAY);
    }

    private PayUCheckoutProConfig getCheckoutProConfig() {
        PayUCheckoutProConfig checkoutProConfig = new PayUCheckoutProConfig();
        checkoutProConfig.setPaymentModesOrder(getCheckoutOrderList());
        checkoutProConfig.setMerchantName(getString(R.string.app_name));
        checkoutProConfig.setMerchantLogo(R.drawable.logo);
        checkoutProConfig.setWaitingTime(30000);
        checkoutProConfig.setMerchantResponseTimeout(30000);
        return checkoutProConfig;
    }

    private ArrayList<PaymentMode> getCheckoutOrderList() {
        ArrayList<PaymentMode> checkoutOrderList = new ArrayList();
        checkoutOrderList.add(new PaymentMode(PaymentType.UPI, PayUCheckoutProConstants.CP_GOOGLE_PAY));
        checkoutOrderList.add(new PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PHONEPE));
        checkoutOrderList.add(new PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PAYTM));
        return checkoutOrderList;
    }

    private PayUPaymentParams preparePayUBizParams() {
        AppEnvironment appEnvironment = ((BaseApplication) getApplication()).getAppEnvironment();
        HashMap<String, Object> additionalParams = new HashMap<>();
        additionalParams.put(PayUCheckoutProConstants.CP_UDF1, "udf1");
        additionalParams.put(PayUCheckoutProConstants.CP_UDF2, "udf2");
        additionalParams.put(PayUCheckoutProConstants.CP_UDF3, "udf3");
        additionalParams.put(PayUCheckoutProConstants.CP_UDF4, "udf4");
        additionalParams.put(PayUCheckoutProConstants.CP_UDF5, "udf5");
        PayUPaymentParams.Builder builder = new PayUPaymentParams.Builder();
        builder.setAmount(payAmount+"")
                .setIsProduction(!appEnvironment.debug())
                .setProductInfo(packageName)
                .setKey(appEnvironment.merchant_Key())
                .setPhone(Utils.getPhone(this))
                .setTransactionId(transId)
                .setFirstName(Utils.getName(this))
                .setEmail(Utils.getEmail(this))
                .setSurl(appEnvironment.surl())
                .setFurl(appEnvironment.furl())
                .setUserCredential(appEnvironment.merchant_Key() + ":john@yopmail.com")
                .setAdditionalParams(additionalParams);
        PayUPaymentParams payUPaymentParams = builder.build();
        return payUPaymentParams;
    }

    private String calculateHash(String hashString) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(hashString.getBytes());
            byte[] mdbytes = messageDigest.digest();
            return getHexString(mdbytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getHexString(byte[] array) {
        StringBuilder hash = new StringBuilder();
        for (byte hashByte : array) {
            hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }
        return hash.toString();
    }

    private String calculateHmacSHA1Hash(String data, String key) {
        String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        String result = null;

        try {
            Key signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = getHexString(rawHmac);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult called requestCode = "+requestCode+", resultCode = "+resultCode);
        if(requestCode == 100 && resultCode == RESULT_OK){
            if(data != null){
                String promoCode = data.getStringExtra("promoCode");
                if(Utils.isValidString(promoCode)){
                    ((EditText)findViewById(R.id.promoCode)).setText(promoCode);
                    applyPromoCode();
                }
            }
        }
    }

    private void updatePaymentStatus(Object transactionResponse){
        try{
            HashMap<String, Object> response = (HashMap<String, Object>) transactionResponse;
            JSONObject result = new JSONObject((String)response.get("payuResponse"));
            if(result.has("result")) {
                result = result.optJSONObject("result");
            }
            String payuResponse = result.optString("status");
            PaymentStatus paymentStatus = new PaymentStatus();
            if ("success".equalsIgnoreCase(payuResponse)) {
                paymentStatus.status = PAYMENT_SUCCESS;
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            }else if("failed".equalsIgnoreCase(payuResponse)){
                paymentStatus.status = PAYMENT_FAILED;
            }else{
                paymentStatus.status = PAYMENT_DECLINE;
            }
            paymentStatus.paymentID = result.optString("id", result.optString("mihpayid"));
            paymentStatus.paymentMode = result.optString("mode");
            paymentStatus.bankRefNo = result.optString("bank_ref_no");
            paymentStatus.pGtype = result.optString("PG_TYPE");
            paymentStatus.discount = Math.round(discount);
            paymentStatus.txnid = transId;
            paymentStatus.payStatus = payuResponse;
            String message = result.optString("field9");
            Log.i("updatePaymentStatus", "result ="+result);
            Log.i("updatePaymentStatus", "paymentStatus ="+paymentStatus);
            Log.i("updatePaymentStatus", "payuResponse ="+payuResponse);
            PaymentUtils.UpdateTransactionStatus(this, paymentStatus, new CompleteListener() {
                @Override
                public void success(JSONObject response) {
                    if ("success".equalsIgnoreCase(payuResponse)) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        PaymentUtils.removeCart(CartActivity.this, -1, new CompleteListener() {
                            @Override
                            public void success(JSONObject response) {
                                Preferences.put(getApplicationContext(), Preferences.KEY_CART_COUNT, 0);
                                if("class".equalsIgnoreCase(type)){
                                    Utils.openMyPackagesNewTask(CartActivity.this, 0);
                                }else if("test".equalsIgnoreCase(type)){
                                    Utils.openMyPackagesNewTask(CartActivity.this, 1);
                                }else{
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            }

                            @Override
                            public void error(String error) {
                            }
                        });
                    }
                }

                @Override
                public void error(String error) {
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    private void sendPaymentStatus(final int status){
        PaymentStatus paymentStatus = new PaymentStatus();
        paymentStatus.discount = Math.round(discount);
        paymentStatus.txnid = transId;
        paymentStatus.status = status;
        PaymentUtils.UpdateTransactionStatus(this, paymentStatus, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(status == PAYMENT_SUCCESS) {
                    Toast.makeText(getApplicationContext(), "Transaction completed succesfully", Toast.LENGTH_SHORT).show();
                    PaymentUtils.removeCart(CartActivity.this, -1, new CompleteListener() {
                        @Override
                        public void success(JSONObject response) {
                            Preferences.put(getApplicationContext(), Preferences.KEY_CART_COUNT, 0);
                            if("class".equalsIgnoreCase(type)){
                                Utils.openMyPackagesNewTask(CartActivity.this, 0);
                            }else if("test".equalsIgnoreCase(type)){
                                Utils.openMyPackagesNewTask(CartActivity.this, 1);
                            }else{
                                setResult(RESULT_OK);
                                finish();
                            }
                        }

                        @Override
                        public void error(String error) {

                        }
                    });
                }
            }
            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyPromoCode(){
        promoCodeValue = ((EditText)findViewById(R.id.promoCode)).getText().toString().trim();
        if(Utils.isValidString(promoCodeValue)){
            hideKeypad();
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            PaymentUtils.applypromo(this, promoCodeValue, totalamount, new CompleteListener() {
                @Override
                public void success(JSONObject response) {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    response = response.optJSONObject("Body");
                    discount = response.optDouble("discount_amount");
                    originalAmount = amount;
                    originalPayAmount = payAmount;
                    originalTotalamount = totalamount;
                    setPriceView();
                    appliedPromoCode();
                }

                @Override
                public void error(String error) {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(getApplicationContext(), "Invalid Promo Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPromoCode(){
        amount = originalAmount;
        payAmount = originalPayAmount;
        discount = 0;
        totalamount = originalTotalamount;
        setPriceView();
        findViewById(R.id.promoCodeLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.appliedLayout).setVisibility(View.GONE);
        promoCodeValue = "";
    }

    private void appliedPromoCode(){
        findViewById(R.id.promoCodeLayout).setVisibility(View.GONE);
        findViewById(R.id.appliedLayout).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.AppliedpromoCode)).setText(promoCodeValue);
    }
    private void hideKeypad(){
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(findViewById(R.id.promoCode).getWindowToken(), 0);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            hideKeyboard();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() {
        try {
            View view = getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Exception e) {}
    }

    private void getPromoCoupon(){
        TestUtils.getPromotions(this, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                String message = response.optString("Message");
                if(Utils.isValidString(message)) {
                    ((TextView) findViewById(R.id.promoCoupon)).setText(message);
                    ((TextView) findViewById(R.id.promoCoupon)).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void error(String error) {

            }
        });
    }

    private void offer(){
        Intent intent = new Intent(this, OfferDialogActivity.class);
        startActivityForResult(intent, 100);
    }
}
