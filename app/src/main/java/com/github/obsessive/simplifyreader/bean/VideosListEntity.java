/*
 * Copyright (c) 2015 [1076559197@qq.com | tchen0707@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.obsessive.simplifyreader.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/4/9.
 * Description:
 */
public class VideosListEntity implements Parcelable {
    private String id;
    private String title;
    private String link;
    private String thumbnail;
    private String thumbnail_v2;
    private String duration;
    private String category;
    private String tags;
    private String state;
    private int view_count;
    private int favorite_count;
    private int comment_count;
    private int up_count;
    private int down_count;
    private String published;
    private VideosListUserEntity user;
    private String public_type;
    private String paid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getThumbnail_v2() {
        return thumbnail_v2;
    }

    public void setThumbnail_v2(String thumbnail_v2) {
        this.thumbnail_v2 = thumbnail_v2;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getView_count() {
        return view_count;
    }

    public void setView_count(int view_count) {
        this.view_count = view_count;
    }

    public int getFavorite_count() {
        return favorite_count;
    }

    public void setFavorite_count(int favorite_count) {
        this.favorite_count = favorite_count;
    }

    public int getComment_count() {
        return comment_count;
    }

    public void setComment_count(int comment_count) {
        this.comment_count = comment_count;
    }

    public int getUp_count() {
        return up_count;
    }

    public void setUp_count(int up_count) {
        this.up_count = up_count;
    }

    public int getDown_count() {
        return down_count;
    }

    public void setDown_count(int down_count) {
        this.down_count = down_count;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public VideosListUserEntity getUser() {
        return user;
    }

    public void setUser(VideosListUserEntity user) {
        this.user = user;
    }

    public String getPublic_type() {
        return public_type;
    }

    public void setPublic_type(String public_type) {
        this.public_type = public_type;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.link);
        dest.writeString(this.thumbnail);
        dest.writeString(this.thumbnail_v2);
        dest.writeString(this.duration);
        dest.writeString(this.category);
        dest.writeString(this.tags);
        dest.writeString(this.state);
        dest.writeInt(this.view_count);
        dest.writeInt(this.favorite_count);
        dest.writeInt(this.comment_count);
        dest.writeInt(this.up_count);
        dest.writeInt(this.down_count);
        dest.writeString(this.published);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.public_type);
        dest.writeString(this.paid);
    }

    public VideosListEntity() {
    }

    protected VideosListEntity(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.link = in.readString();
        this.thumbnail = in.readString();
        this.thumbnail_v2 = in.readString();
        this.duration = in.readString();
        this.category = in.readString();
        this.tags = in.readString();
        this.state = in.readString();
        this.view_count = in.readInt();
        this.favorite_count = in.readInt();
        this.comment_count = in.readInt();
        this.up_count = in.readInt();
        this.down_count = in.readInt();
        this.published = in.readString();
        this.user = in.readParcelable(VideosListUserEntity.class.getClassLoader());
        this.public_type = in.readString();
        this.paid = in.readString();
    }

    public static final Parcelable.Creator<VideosListEntity> CREATOR = new Parcelable.Creator<VideosListEntity>() {
        public VideosListEntity createFromParcel(Parcel source) {
            return new VideosListEntity(source);
        }

        public VideosListEntity[] newArray(int size) {
            return new VideosListEntity[size];
        }
    };
}
