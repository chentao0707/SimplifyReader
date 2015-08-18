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
public class VideosListUserEntity implements Parcelable {
    private int id;
    private String name;
    private String link;
    private String gender;
    private String avatar;
    private String avatar_large;
    private String description;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.link);
        dest.writeString(this.gender);
        dest.writeString(this.avatar);
        dest.writeString(this.avatar_large);
        dest.writeString(this.description);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar_large() {
        return avatar_large;
    }

    public void setAvatar_large(String avatar_large) {
        this.avatar_large = avatar_large;
    }

    public VideosListUserEntity() {
    }

    protected VideosListUserEntity(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.link = in.readString();
        this.gender = in.readString();
        this.avatar = in.readString();
        this.avatar_large = in.readString();
        this.description = in.readString();
    }

    public static final Parcelable.Creator<VideosListUserEntity> CREATOR = new Parcelable.Creator<VideosListUserEntity>() {
        public VideosListUserEntity createFromParcel(Parcel source) {
            return new VideosListUserEntity(source);
        }

        public VideosListUserEntity[] newArray(int size) {
            return new VideosListUserEntity[size];
        }
    };

    @Override
    public String toString() {
        return "VideosListUserEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", link='" + link + '\'' +
                ", gender='" + gender + '\'' +
                ", avatar='" + avatar + '\'' +
                ", avatar_large='" + avatar_large + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
