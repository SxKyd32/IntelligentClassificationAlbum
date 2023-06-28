package com.example.photosortingsystem.entity;


/**
 * 内存中的元素
 */
public class MemoryItem {
    private String imageId;
    private String type;
    public MemoryItem(String imageId, String type) {
        this.imageId = imageId;
        this.type = type;
    }
    public String getImageId() {
        return imageId;
    }
    public String getType() { return type; }
}
