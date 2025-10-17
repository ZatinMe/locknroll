package com.locknroll.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FruitTransaction Entity - Stored in MongoDB
 * Represents transaction logs for fruit operations
 */
@Document(collection = "fruit_transactions")
public class FruitTransaction {
    
    @Id
    private String id;
    
    @Field("fruit_id")
    private Long fruitId;
    
    @Field("fruit_name")
    private String fruitName;
    
    @Field("operation_type")
    private String operationType; // CREATE, UPDATE, DELETE, PURCHASE, RESTOCK
    
    @Field("quantity_change")
    private Integer quantityChange;
    
    @Field("price_change")
    private BigDecimal priceChange;
    
    @Field("old_quantity")
    private Integer oldQuantity;
    
    @Field("new_quantity")
    private Integer newQuantity;
    
    @Field("old_price")
    private BigDecimal oldPrice;
    
    @Field("new_price")
    private BigDecimal newPrice;
    
    @Field("user_id")
    private String userId;
    
    @Field("session_id")
    private String sessionId;
    
    @Field("thread_name")
    private String threadName;
    
    @Field("timestamp")
    private LocalDateTime timestamp;
    
    @Field("lock_acquired")
    private Boolean lockAcquired;
    
    @Field("lock_duration_ms")
    private Long lockDurationMs;
    
    @Field("cache_hit")
    private Boolean cacheHit;
    
    // Constructors
    public FruitTransaction() {
        this.timestamp = LocalDateTime.now();
    }
    
    public FruitTransaction(Long fruitId, String fruitName, String operationType) {
        this();
        this.fruitId = fruitId;
        this.fruitName = fruitName;
        this.operationType = operationType;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Long getFruitId() {
        return fruitId;
    }
    
    public void setFruitId(Long fruitId) {
        this.fruitId = fruitId;
    }
    
    public String getFruitName() {
        return fruitName;
    }
    
    public void setFruitName(String fruitName) {
        this.fruitName = fruitName;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public Integer getQuantityChange() {
        return quantityChange;
    }
    
    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }
    
    public BigDecimal getPriceChange() {
        return priceChange;
    }
    
    public void setPriceChange(BigDecimal priceChange) {
        this.priceChange = priceChange;
    }
    
    public Integer getOldQuantity() {
        return oldQuantity;
    }
    
    public void setOldQuantity(Integer oldQuantity) {
        this.oldQuantity = oldQuantity;
    }
    
    public Integer getNewQuantity() {
        return newQuantity;
    }
    
    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }
    
    public BigDecimal getOldPrice() {
        return oldPrice;
    }
    
    public void setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;
    }
    
    public BigDecimal getNewPrice() {
        return newPrice;
    }
    
    public void setNewPrice(BigDecimal newPrice) {
        this.newPrice = newPrice;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getThreadName() {
        return threadName;
    }
    
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Boolean getLockAcquired() {
        return lockAcquired;
    }
    
    public void setLockAcquired(Boolean lockAcquired) {
        this.lockAcquired = lockAcquired;
    }
    
    public Long getLockDurationMs() {
        return lockDurationMs;
    }
    
    public void setLockDurationMs(Long lockDurationMs) {
        this.lockDurationMs = lockDurationMs;
    }
    
    public Boolean getCacheHit() {
        return cacheHit;
    }
    
    public void setCacheHit(Boolean cacheHit) {
        this.cacheHit = cacheHit;
    }
    
    @Override
    public String toString() {
        return "FruitTransaction{" +
                "id='" + id + '\'' +
                ", fruitId=" + fruitId +
                ", fruitName='" + fruitName + '\'' +
                ", operationType='" + operationType + '\'' +
                ", quantityChange=" + quantityChange +
                ", priceChange=" + priceChange +
                ", oldQuantity=" + oldQuantity +
                ", newQuantity=" + newQuantity +
                ", oldPrice=" + oldPrice +
                ", newPrice=" + newPrice +
                ", userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", threadName='" + threadName + '\'' +
                ", timestamp=" + timestamp +
                ", lockAcquired=" + lockAcquired +
                ", lockDurationMs=" + lockDurationMs +
                ", cacheHit=" + cacheHit +
                '}';
    }
}
