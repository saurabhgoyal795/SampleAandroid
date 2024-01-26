package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.os.Parcel;
import android.os.Parcelable;

public class QuestionItem implements Parcelable {
    public int subjectId;
    public int questionId;
    public int examQuesId;
    public int quesOptionID;
    public boolean isVisited;
    public boolean isReviewed;
    public boolean isAttempted;
    public String data;
    public int sectionIndex;

    public QuestionItem(){}
    protected QuestionItem(Parcel in) {
        subjectId = in.readInt();
        questionId = in.readInt();
        examQuesId = in.readInt();
        quesOptionID = in.readInt();
        isVisited = in.readByte() != 0;
        isReviewed = in.readByte() != 0;
        isAttempted = in.readByte() != 0;
        data = in.readString();
        sectionIndex = in.readInt();
    }

    public static final Creator<QuestionItem> CREATOR = new Creator<QuestionItem>() {
        @Override
        public QuestionItem createFromParcel(Parcel in) {
            return new QuestionItem(in);
        }

        @Override
        public QuestionItem[] newArray(int size) {
            return new QuestionItem[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj == this)
            return true;

        if(!(obj instanceof QuestionItem)){
            return false;
        }
        QuestionItem item = (QuestionItem)obj;
        return this.examQuesId == item.examQuesId && this.subjectId == item.subjectId && this.questionId == item.questionId;
    }

    @Override
    public int hashCode() {
        return (examQuesId + subjectId + questionId)+"".hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(subjectId);
        dest.writeInt(questionId);
        dest.writeInt(examQuesId);
        dest.writeInt(quesOptionID);
        dest.writeByte((byte) (isVisited ? 1 : 0));
        dest.writeByte((byte) (isReviewed ? 1 : 0));
        dest.writeByte((byte) (isAttempted ? 1 : 0));
        dest.writeString(data);
        dest.writeInt(sectionIndex);
    }
}
