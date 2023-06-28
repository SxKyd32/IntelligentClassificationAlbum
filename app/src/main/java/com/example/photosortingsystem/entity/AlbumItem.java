package com.example.photosortingsystem.entity;


/**
 * 相册中的元素
 */
public class AlbumItem {
        private String name;
        private String url;
        public AlbumItem(String name, String url) {
            this.name = name;
            this.url = url;

        }
        public String getName() {
            return name;
        }
        public String getImageId() {
            return url;
        }
}
