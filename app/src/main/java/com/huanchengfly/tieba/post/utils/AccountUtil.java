package com.huanchengfly.tieba.post.utils;

import android.content.Context;
import android.content.Intent;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.api.Error;
import com.huanchengfly.tieba.post.api.TiebaApi;
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback;
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException;
import com.huanchengfly.tieba.post.models.MyInfoBean;
import com.huanchengfly.tieba.post.models.database.Account;

import org.litepal.LitePal;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountUtil {
    public static final String TAG = "AccountUtil";
    public static final String ACTION_SWITCH_ACCOUNT = "com.huanchengfly.tieba.post.action.SWITCH_ACCOUNT";

    @Nullable
    public static Account getLoginInfo(@NonNull Context context) {
        int loginUser = context.getSharedPreferences("accountData", Context.MODE_PRIVATE).getInt("now", -1);
        if (loginUser == -1) {
            return null;
        }
        return getAccountInfo(loginUser);
    }

    public static List<Account> getAllAccounts() {
        return LitePal.findAll(Account.class);
    }

    public static Account getAccountInfo(int accountId) {
        return LitePal.where("id = ?", String.valueOf(accountId)).findFirst(Account.class);
    }

    public static Account getAccountInfoByUid(@NonNull String uid) {
        return LitePal.where("uid = ?", uid).findFirst(Account.class);
    }

    public static Account getAccountInfoByBduss(@NonNull String bduss) {
        return LitePal.where("bduss = ?", bduss).findFirst(Account.class);
    }

    public static boolean isLoggedIn(@NonNull Context context) {
        return (getLoginInfo(context) != null);
    }

    public static boolean newAccount(@NonNull Context context, @NonNull Account account, boolean needSwitch) {
        if (account.save()) {
            if (needSwitch) {
                return switchUser(context, account.getId());
            }
            return true;
        }
        return false;
    }

    public static boolean switchUser(@NonNull Context context, int id) {
        context.sendBroadcast(new Intent().setAction(ACTION_SWITCH_ACCOUNT));
        return context.getSharedPreferences("accountData", Context.MODE_PRIVATE).edit().putInt("now", id).commit();
    }

    public static void updateUserInfo(@NonNull Context context, CommonCallback<MyInfoBean> commonCallback) {
        Account account = getLoginInfo(context);
        if (account == null) {
            commonCallback.onFailure(Error.ERROR_NOT_LOGGED_IN, "未登录");
            return;
        }
        updateUserInfoByBduss(context, account.getBduss(), commonCallback);
    }

    public static boolean updateLoginInfo(@NonNull String cookie) {
        String[] bdussSplit = cookie.split("BDUSS=");
        String[] sTokenSplit = cookie.split("STOKEN=");
        if (bdussSplit.length > 1 && sTokenSplit.length > 1) {
            String bduss = bdussSplit[1].split(";")[0];
            String sToken = sTokenSplit[1].split(";")[0];
            Account account = getAccountInfoByBduss(bduss);
            if (account != null) {
                account.setsToken(sToken)
                        .setCookie(cookie)
                        .update(account.getId());
                return true;
            }
        }
        return false;
    }

    public static void updateUserInfoByBduss(@NonNull Context context, @NonNull String bduss, @Nullable CommonCallback<MyInfoBean> commonCallback) {
        TiebaApi.getInstance().myInfo(AccountUtil.getBdussCookie(bduss)).enqueue(new Callback<MyInfoBean>() {
            @Override
            public void onResponse(@NonNull Call<MyInfoBean> call, @NonNull Response<MyInfoBean> response) {
                MyInfoBean myInfoBean = response.body();
                if (myInfoBean == null) {
                    if (commonCallback != null)
                        commonCallback.onFailure(Error.ERROR_UNKNOWN, "未知错误");
                    return;
                }
                if (!myInfoBean.getData().isLogin()) {
                    if (commonCallback != null)
                        commonCallback.onFailure(Error.ERROR_LOGGED_IN_EXPIRED, "登录已过期");
                    return;
                }
                String id = String.valueOf(myInfoBean.getData().getUid());
                Account account = getAccountInfoByBduss(bduss);
                if (account == null) account = getAccountInfoByUid(id);
                if (account == null) {
                    account = new Account()
                            .setBduss(bduss)
                            .setPortrait(myInfoBean.getData().getAvatarUrl())
                            .setUid(id)
                            .setTbs(myInfoBean.getData().getTbs())
                            .setItbTbs(myInfoBean.getData().getItbTbs())
                            .setName(myInfoBean.getData().getName())
                            .setNameShow(myInfoBean.getData().getShowName());
                    account.save();
                    return;
                }
                account.setBduss(bduss)
                        .setPortrait(myInfoBean.getData().getAvatarUrl())
                        .setTbs(myInfoBean.getData().getTbs())
                        .setItbTbs(myInfoBean.getData().getItbTbs())
                        .setUid(String.valueOf(myInfoBean.getData().getUid()))
                        .setName(myInfoBean.getData().getName())
                        .setNameShow(myInfoBean.getData().getShowName())
                        .update(account.getId());
                if (commonCallback != null) {
                    commonCallback.onSuccess(myInfoBean);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyInfoBean> call, @NonNull Throwable t) {
                if (commonCallback != null) {
                    if (t instanceof TiebaException) {
                        commonCallback.onFailure(((TiebaException) t).getCode(), t.getMessage());
                    } else {
                        commonCallback.onFailure(-1, t.getMessage());
                    }
                }
            }
        });
    }

    @SuppressWarnings("ApplySharedPref")
    public static void exit(@NonNull Context context) {
        List<Account> accounts = getAllAccounts();
        Account account = getLoginInfo(context);
        if (account == null) return;
        account.delete();
        CookieManager.getInstance().removeAllCookies(null);
        if (accounts.size() > 1) {
            accounts = getAllAccounts();
            account = accounts.get(0);
            switchUser(context, account.getId());
            Toast.makeText(context, "退出登录成功，已切换至账号 " + account.getNameShow(), Toast.LENGTH_SHORT).show();
            return;
        }
        context.getSharedPreferences("accountData", Context.MODE_PRIVATE).edit().clear().commit();
        Toast.makeText(context, R.string.toast_exit_account_success, Toast.LENGTH_SHORT).show();
    }

    @Nullable
    public static String getSToken(Context context) {
        if (context == null) return null;
        Account account = AccountUtil.getLoginInfo(context);
        if (account != null) {
            return account.getsToken();
        }
        return null;
    }

    @Nullable
    public static String getCookie(Context context) {
        if (context == null) return null;
        Account account = AccountUtil.getLoginInfo(context);
        if (account != null) {
            return account.getCookie();
        }
        return null;
    }

    @Nullable
    public static String getUid(Context context) {
        if (context == null) return null;
        Account account = AccountUtil.getLoginInfo(context);
        if (account != null) {
            return account.getUid();
        }
        return null;
    }

    @Nullable
    public static String getBduss(Context context) {
        if (context == null) return null;
        Account account = AccountUtil.getLoginInfo(context);
        if (account != null) {
            return account.getBduss();
        }
        return null;
    }

    @Nullable
    public static String getBdussCookie(Context context) {
        if (context == null) return null;
        String bduss = getBduss(context);
        if (bduss != null) {
            return getBdussCookie(bduss);
        }
        return null;
    }

    @NonNull
    public static String getBdussCookie(@NonNull String bduss) {
        return "BDUSS=" + bduss + "; path=/; domain=.baidu.com; httponly";
    }
}