package org.wordpress.android.fluxc.persistence;

import com.wellsql.generated.PostModelTable;
import com.yarolegovich.wellsql.SelectQuery;
import com.yarolegovich.wellsql.WellSql;

import org.wordpress.android.fluxc.model.PostModel;
import org.wordpress.android.fluxc.model.SiteModel;

import java.util.Collections;
import java.util.List;

public class PostSqlUtils {
    public static int insertOrUpdatePost(PostModel post, boolean overwriteLocalChanges) {
        if (post == null) {
            return 0;
        }

        List<PostModel> postResult;
        if (post.isLocalDraft()) {
            postResult = WellSql.select(PostModel.class)
                    .where()
                    .equals(PostModelTable.ID, post.getId())
                    .endWhere().getAsModel();
        } else {
            postResult = WellSql.select(PostModel.class)
                    .where().beginGroup()
                    .equals(PostModelTable.ID, post.getId())
                    .or()
                    .beginGroup()
                    .equals(PostModelTable.REMOTE_POST_ID, post.getRemotePostId())
                    .equals(PostModelTable.LOCAL_SITE_ID, post.getLocalSiteId())
                    .endGroup()
                    .endGroup().endWhere().getAsModel();
        }

        if (postResult.isEmpty()) {
            // insert
            WellSql.insert(post).asSingleTransaction(true).execute();
            return 1;
        } else {
            if (postResult.size() > 1) {
                // We've ended up with a duplicate entry, probably due to a push/fetch race condition
                // One matches based on local ID (this is the one we're trying to update with a remote post ID)
                // The other matches based on local site ID + remote post ID, and we got it from a fetch
                // Just remove the entry without a remote post ID (the one matching the current post's local ID)
                return WellSql.delete(PostModel.class).whereId(post.getId());
            }
            // Update only if local changes for this post don't exist
            if (overwriteLocalChanges || !postResult.get(0).isLocallyChanged()) {
                int oldId = postResult.get(0).getId();
                return WellSql.update(PostModel.class).whereId(oldId)
                        .put(post, new UpdateAllExceptId<>(PostModel.class)).execute();
            }
        }
        return 0;
    }

    public static int insertOrUpdatePostKeepingLocalChanges(PostModel post) {
        return insertOrUpdatePost(post, false);
    }

    public static int insertOrUpdatePostOverwritingLocalChanges(PostModel post) {
        return insertOrUpdatePost(post, true);
    }

    public static List<PostModel> getPostsForSite(SiteModel site, boolean getPages) {
        if (site == null) {
            return Collections.emptyList();
        }

        return WellSql.select(PostModel.class)
                .where().beginGroup()
                .equals(PostModelTable.LOCAL_SITE_ID, site.getId())
                .equals(PostModelTable.IS_PAGE, getPages)
                .endGroup().endWhere()
                .orderBy(PostModelTable.IS_LOCAL_DRAFT, SelectQuery.ORDER_DESCENDING)
                .orderBy(PostModelTable.DATE_CREATED, SelectQuery.ORDER_DESCENDING)
                .getAsModel();
    }

    public static List<PostModel> getPostsForSiteWithFormat(SiteModel site, List<String> postFormat, boolean getPages) {
        if (site == null) {
            return Collections.emptyList();
        }

        return WellSql.select(PostModel.class)
                .where().beginGroup()
                .equals(PostModelTable.LOCAL_SITE_ID, site.getId())
                .isIn(PostModelTable.POST_FORMAT, postFormat)
                .equals(PostModelTable.IS_PAGE, getPages)
                .endGroup().endWhere()
                .orderBy(PostModelTable.IS_LOCAL_DRAFT, SelectQuery.ORDER_DESCENDING)
                .orderBy(PostModelTable.DATE_CREATED, SelectQuery.ORDER_DESCENDING)
                .getAsModel();
    }

    public static List<PostModel> getUploadedPostsForSite(SiteModel site, boolean getPages) {
        if (site == null) {
            return Collections.emptyList();
        }

        return WellSql.select(PostModel.class)
                .where().beginGroup()
                .equals(PostModelTable.LOCAL_SITE_ID, site.getId())
                .equals(PostModelTable.IS_PAGE, getPages)
                .equals(PostModelTable.IS_LOCAL_DRAFT, false)
                .endGroup().endWhere()
                .orderBy(PostModelTable.IS_LOCAL_DRAFT, SelectQuery.ORDER_DESCENDING)
                .orderBy(PostModelTable.DATE_CREATED, SelectQuery.ORDER_DESCENDING)
                .getAsModel();
    }

    public static PostModel insertPostForResult(PostModel post) {
        WellSql.insert(post).asSingleTransaction(true).execute();

        return post;
    }

    public static int deletePost(PostModel post) {
        if (post == null) {
            return 0;
        }

        return WellSql.delete(PostModel.class)
                .where().beginGroup()
                .equals(PostModelTable.ID, post.getId())
                .equals(PostModelTable.LOCAL_SITE_ID, post.getLocalSiteId())
                .endGroup()
                .endWhere()
                .execute();
    }

    public static int deleteUploadedPostsForSite(SiteModel site, boolean pages) {
        if (site == null) {
            return 0;
        }

        return WellSql.delete(PostModel.class)
                .where().beginGroup()
                .equals(PostModelTable.LOCAL_SITE_ID, site.getId())
                .equals(PostModelTable.IS_PAGE, pages)
                .equals(PostModelTable.IS_LOCAL_DRAFT, false)
                .equals(PostModelTable.IS_LOCALLY_CHANGED, false)
                .endGroup()
                .endWhere()
                .execute();
    }

    public static int deleteAllPosts() {
        return WellSql.delete(PostModel.class).execute();
    }

    public static boolean getSiteHasLocalChanges(SiteModel site) {
        return site != null && WellSql.select(PostModel.class)
                .where().beginGroup()
                .equals(PostModelTable.LOCAL_SITE_ID, site.getId())
                .beginGroup()
                .equals(PostModelTable.IS_LOCAL_DRAFT, true)
                .or()
                .equals(PostModelTable.IS_LOCALLY_CHANGED, true)
                .endGroup().endGroup().endWhere().getAsCursor().getCount() > 0;
    }

    public static int getNumLocalChanges() {
        return WellSql.select(PostModel.class)
                      .where().beginGroup()
                      .equals(PostModelTable.IS_LOCAL_DRAFT, true)
                      .or()
                      .equals(PostModelTable.IS_LOCALLY_CHANGED, true)
                      .endGroup().endWhere()
                      .getAsCursor().getCount();
    }
}
