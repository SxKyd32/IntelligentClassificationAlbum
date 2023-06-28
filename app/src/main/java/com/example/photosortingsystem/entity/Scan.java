package com.example.photosortingsystem.entity;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * 人脸识别类
 */

public class Scan {
    public String face_num;
    public List<Face> faces;
    public String image_id;
    public String request_id;
    public String time_used;

    @Override
    public String toString() {
        return "Scan{" +
                "face_num='" + face_num + '\'' +
                ", faces=" + faces +
                ", image_id='" + image_id + '\'' +
                ", request_id='" + request_id + '\'' +
                ", time_used='" + time_used + '\'' +
                '}';
    }


    public class Face {
        public String face_token;           //人脸的标识
        public Rectangle face_rectangle;    //人脸矩形框的位置
        public Attributes attributes;       //人脸属性特征

        @Override
        public String toString() {
            return "Face{" +
                    "face_token='" + face_token + '\'' +
                    ", face_rectangle=" + face_rectangle +
                    ", attributes=" + attributes +
                    '}';
        }


        public String getFaceToken() {
            return face_token;
        }

        public class Rectangle {
            public String top;
            public String left;
            public String width;
            public String height;
        }


        public class Attributes {
            public Gender gender;           //性别分析结果
            public Age age;                 //年龄分析结果
            public Beauty beauty;           //颜值分析结果
            public SkinStatus skinstatus;   //面部特征识别结果

            @Override
            public String toString() {
                return "Attributes{" +
                        "gender=" + gender +
                        ", age=" + age +
                        ", beauty=" + beauty +
                        ", skinstatus=" + skinstatus +
                        '}';
            }

            public  class Gender {
                public String value;
            }

            public  class Age {
                public String value;
            }

            public  class Beauty {
                public String male_score;
                public String female_score;
            }

            public  class SkinStatus {
                public String health;           //健康
                public String stain;            //色斑
                public String acne;             //青春痘
                public String dark_circle;      //黑眼圈
            }
        }

    }

}
