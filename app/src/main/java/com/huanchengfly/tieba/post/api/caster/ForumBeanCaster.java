package com.huanchengfly.tieba.post.api.caster;

import android.webkit.URLUtil;

import com.huanchengfly.tieba.post.BaseApplication;
import com.huanchengfly.tieba.post.api.models.ForumPageBean;
import com.huanchengfly.tieba.post.api.models.web.ForumBean;

import java.util.ArrayList;
import java.util.List;

public class ForumBeanCaster extends ICaster<ForumBean, ForumPageBean> {
    @Override
    public ForumPageBean cast(ForumBean forumBean) {
        ForumPageBean forumPageBean = new ForumPageBean();
        ForumBean.FrsDataBean frsDataBean = forumBean.getData().getFrsData();
        forumPageBean.setErrorCode(String.valueOf(forumBean.getErrorCode()));
        forumPageBean.setErrorMsg(forumBean.getErrorMsg());
        forumPageBean.setAnti(frsDataBean.getAnti());
        forumPageBean.setPage(new FrsPageBeanImpl().cast(frsDataBean.getPage()));
        forumPageBean.setForum(new FrsForumBeanImpl().cast(frsDataBean.getForum()));
        forumPageBean.setUser(new FrsUserBeanImpl().cast(frsDataBean.getUser()));
        List<ForumPageBean.ThreadBean> threadBeans = new ArrayList<>();
        List<ForumPageBean.UserBean> userBeans = new ArrayList<>();
        FrsThreadBeanImpl frsThreadBeanImpl = new FrsThreadBeanImpl();
        for (ForumBean.FrsThreadBean frsThreadBean : frsDataBean.getThreadList()) {
            if (frsThreadBean.getLastTimeInt() != null) {
                threadBeans.add(frsThreadBeanImpl.cast(frsThreadBean));
                userBeans.add(frsThreadBean.getAuthor());
            }
        }
        forumPageBean.setThreadList(threadBeans);
        forumPageBean.setUserList(userBeans);
        return forumPageBean;
    }

    private static class FrsThreadBeanImpl extends ICaster<ForumBean.FrsThreadBean, ForumPageBean.ThreadBean> {
        @Override
        public ForumPageBean.ThreadBean cast(ForumBean.FrsThreadBean frsThreadBean) {
            ForumPageBean.ThreadBean threadBean = new ForumPageBean.ThreadBean();
            List<ForumPageBean.AbstractBean> abstractBeans = new ArrayList<>();
            List<ForumPageBean.MediaInfoBean> mediaInfoBeans = new ArrayList<>();
            MediaBeanImpl mediaBeanImpl = new MediaBeanImpl();
            for (ForumBean.MediaBean mediaBean : frsThreadBean.getMedia()) {
                ForumPageBean.MediaInfoBean mediaInfoBean = mediaBeanImpl.cast(mediaBean);
                if (mediaInfoBean != null) mediaInfoBeans.add(mediaBeanImpl.cast(mediaBean));
            }
            abstractBeans.add(new ForumPageBean.AbstractBean("0", frsThreadBean.getAbstracts()));
            int agreeNum = 0;
            if (frsThreadBean.getAgree() != null) {
                agreeNum = frsThreadBean.getAgree().getAgreeNum();
            }
            threadBean.setAbstractString(frsThreadBean.getAbstracts())
                    .setAbstractBeans(abstractBeans)
                    .setAgreeNum(String.valueOf(agreeNum))
                    .setAuthorId(frsThreadBean.getAuthor().getId())
                    .setId(frsThreadBean.getId())
                    .setIsGood(frsThreadBean.getIsGood())
                    .setIsNoTitle(frsThreadBean.getIsNoTitle())
                    .setIsTop(frsThreadBean.getIsTop())
                    .setLastTime(frsThreadBean.getLastTime())
                    .setLastTimeInt(frsThreadBean.getLastTimeInt())
                    .setReplyNum(frsThreadBean.getReplyNum())
                    .setTid(frsThreadBean.getTid())
                    .setTitle(frsThreadBean.getTitle())
                    .setVideoInfo(frsThreadBean.getVideoInfo() != null ? frsThreadBean.getVideoInfo().setVideoUrl(frsThreadBean.getVideoInfo().getOriginVideoUrl()) : null)
                    .setMedia(mediaInfoBeans)
                    .setViewNum(frsThreadBean.getViewNum());
            return threadBean;
        }
    }

    private static class FrsPageBeanImpl extends ICaster<ForumBean.FrsPageBean, ForumPageBean.PageBean> {
        @Override
        public ForumPageBean.PageBean cast(ForumBean.FrsPageBean frsPageBean) {
            ForumPageBean.PageBean pageBean = new ForumPageBean.PageBean();
            pageBean.setCurGoodId(String.valueOf(frsPageBean.getCurGoodId()));
            pageBean.setCurrentPage(String.valueOf(frsPageBean.getCurrentPage()));
            pageBean.setHasMore(frsPageBean.getCurrentPage() < frsPageBean.getTotalPage() ? "1" : "0");
            pageBean.setHasPrev(frsPageBean.getCurrentPage() > 1 ? "1" : "0");
            pageBean.setOffset(String.valueOf(frsPageBean.getOffset()));
            pageBean.setPageSize(String.valueOf(frsPageBean.getPageSize()));
            pageBean.setTotalCount(String.valueOf(frsPageBean.getTotalCount()));
            pageBean.setTotalPage(String.valueOf(frsPageBean.getTotalPage()));
            return pageBean;
        }
    }

    private static class FrsForumBeanImpl extends ICaster<ForumBean.FrsForumBean, ForumPageBean.ForumBean> {
        @Override
        public ForumPageBean.ForumBean cast(ForumBean.FrsForumBean frsForumBean) {
            ForumPageBean.ForumBean forumBean = new ForumPageBean.ForumBean();
            forumBean.setAvatar(frsForumBean.getAvatar());
            forumBean.setCurScore(frsForumBean.getCurScore());
            forumBean.setGoodClassify(frsForumBean.getGoodClassify());
            forumBean.setId(frsForumBean.getId());
            forumBean.setIsExists(frsForumBean.isExists() ? "1" : "0");
            forumBean.setIsLike(frsForumBean.getIsLike());
            forumBean.setLevelId(frsForumBean.getLevelId());
            forumBean.setLevelName(frsForumBean.getLevelName());
            forumBean.setLevelUpScore(frsForumBean.getLevelUpScore());
            forumBean.setName(frsForumBean.getName());
            forumBean.setSignInInfo(frsForumBean.getSignInInfo());
            forumBean.setTids(frsForumBean.getTids());
            forumBean.setSlogan(frsForumBean.getSlogan());
            forumBean.setUserLevel(frsForumBean.getUserLevel());
            forumBean.setMemberNum(frsForumBean.getMemberNum());
            forumBean.setPostNum(frsForumBean.getPostNum());
            forumBean.setThreadNum(frsForumBean.getThreadNum());
            forumBean.setManagers(frsForumBean.getManagers());
            forumBean.setZyqTitle(frsForumBean.getAttrs().getZyqTitle());
            forumBean.setZyqDefine(frsForumBean.getAttrs().getZyqDefine());
            forumBean.setZyqFriend(frsForumBean.getAttrs().getZyqFriend());
            return forumBean;
        }
    }

    private static class FrsUserBeanImpl extends ICaster<ForumBean.FrsUserBean, ForumPageBean.UserBean> {
        @Override
        public ForumPageBean.UserBean cast(ForumBean.FrsUserBean frsUserBean) {
            ForumPageBean.UserBean userBean = new ForumPageBean.UserBean();
            userBean.setId(frsUserBean.getId());
            if (frsUserBean.getNewUserInfo() == null) {
                userBean.setName(frsUserBean.getName());
                userBean.setNameShow(frsUserBean.getNameShow());
            } else {
                userBean.setName(frsUserBean.getNewUserInfo().getUserName());
                userBean.setNameShow(frsUserBean.getNewUserInfo().getUserNickname());
            }
            userBean.setPortrait(frsUserBean.getPortrait());
            return userBean;
        }
    }

    private static class MediaBeanImpl extends ICaster<ForumBean.MediaBean, ForumPageBean.MediaInfoBean> {
        @Override
        public ForumPageBean.MediaInfoBean cast(ForumBean.MediaBean mediaBean) {
            if (!"pic".equals(mediaBean.getType())) {
                return null;
            }
            ForumPageBean.MediaInfoBean mediaInfoBean = new ForumPageBean.MediaInfoBean();
            String origin = mediaBean.getBigPic();
            String fileName = URLUtil.guessFileName(origin, null, "image/jpeg");
            if (mediaBean.getBigPic().contains(".hiphotos.baidu.com") || mediaBean.getBigPic().contains("imgsrc.baidu.com")) {
                origin = "http://imgsrc.baidu.com/forum/pic/item/" + fileName;
            } else {
                origin = "http://tiebapic.baidu.com/forum/pic/item/" + fileName;
            }
            mediaInfoBean.setBigPic(mediaBean.getBigPic())
                    .setShowOriginalBtn(mediaBean.isGif() ? "0" : "1")
                    .setType("3")
                    .setSrcPic(mediaBean.getStaticImg())
                    .setIsGif(mediaBean.isGif() ? "1" : "0")
                    .setIsLongPic(mediaBean.getHeight() > BaseApplication.ScreenInfo.EXACT_SCREEN_HEIGHT ? "1" : "0")
                    .setOriginPic(origin);
            return mediaInfoBean;
        }
    }
}