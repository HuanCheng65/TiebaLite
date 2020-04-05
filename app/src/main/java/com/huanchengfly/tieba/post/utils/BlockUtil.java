package com.huanchengfly.tieba.post.utils;

import com.huanchengfly.tieba.api.models.ThreadContentBean;
import com.huanchengfly.tieba.post.models.database.Block;
import com.huanchengfly.utils.GsonUtil;

import org.litepal.LitePal;

import java.util.List;

public class BlockUtil {
    private static List<Block> blackList;
    private static List<Block> whiteList;
    //private static Map<String, Boolean> map = new ArrayMap<>();

    private static boolean isInWhiteList(String content) {
        List<Block> list = LitePal.where("type = ? and category = ?", String.valueOf(Block.TYPE_KEYWORD), String.valueOf(Block.CATEGORY_WHITE_LIST)).find(Block.class);
        for (Block block : list) {
            for (String keyword : GsonUtil.getGson().fromJson(block.getKeywords(), String[].class)) {
                if (content.toLowerCase().contains(keyword.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInWhiteList(ThreadContentBean.UserInfoBean userInfo) {
        if (userInfo == null) {
            return false;
        }
        return LitePal.where("uid = ? and category = ?", userInfo.getId(), String.valueOf(Block.CATEGORY_WHITE_LIST)).count(Block.class) > 0;
    }

    private static boolean isInWhiteList(String username, String uid) {
        if (uid != null && LitePal.where("uid = ? and category = ?", uid, String.valueOf(Block.CATEGORY_WHITE_LIST)).count(Block.class) > 0) {
            return true;
        }
        return username != null && LitePal.where("username = ? and category = ?", username, String.valueOf(Block.CATEGORY_WHITE_LIST)).count(Block.class) > 0;
    }

    public static boolean needBlock(String content) {
        List<Block> list = LitePal.where("type = ? and category = ?", String.valueOf(Block.TYPE_KEYWORD), String.valueOf(Block.CATEGORY_BLACK_LIST)).find(Block.class);
        for (Block block : list) {
            for (String keyword : GsonUtil.getGson().fromJson(block.getKeywords(), String[].class)) {
                if (content.toLowerCase().contains(keyword.toLowerCase())) {
                    if (!isInWhiteList(content)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean needBlock(String username, String uid) {
        if (isInWhiteList(username, uid)) {
            return false;
        }
        if (uid != null && LitePal.where("uid = ? and category = ?", uid, String.valueOf(Block.CATEGORY_BLACK_LIST)).count(Block.class) > 0) {
            return true;
        }
        return username != null && LitePal.where("username = ? and category = ?", username, String.valueOf(Block.CATEGORY_BLACK_LIST)).count(Block.class) > 0;
    }

    public static boolean needBlock(ThreadContentBean.UserInfoBean userInfo) {
        return needBlock(userInfo.getName(), userInfo.getId());
    }

    public static List<Block> getBlackList() {
        if (blackList == null) {
            blackList = LitePal.where("category = ?", "10").find(Block.class);
            return blackList;
        }
        LitePal.where("category = ?", "10").findAsync(Block.class).listen((List<Block> list) -> {
            blackList = list;
        });
        return blackList;
    }

    public static List<Block> getWhiteList() {
        if (whiteList == null) {
            whiteList = LitePal.where("category = ?", "11").find(Block.class);
            return whiteList;
        }
        LitePal.where("category = ?", "11").findAsync(Block.class).listen((List<Block> list) -> {
            whiteList = list;
        });
        return whiteList;
    }
}