package io.taskmonk.entities;

public class BatchStatus {
    Integer new_count;
    Integer in_progress;
    Integer completed;
    Integer total;
    public BatchStatus() {

    }
    public BatchStatus(Integer newCount, Integer inProgress, Integer completed, Integer total) {
        this.completed = completed;
        this.new_count = newCount;
        this.in_progress = inProgress;
        this.total = total;
    }

    public Integer getNew_count() {
        return new_count;
    }

    public void setNew_count(Integer new_count) {
        this.new_count = new_count;
    }

    public Integer getIn_progress() {
        return in_progress;
    }

    public void setIn_progress(Integer in_progress) {
        this.in_progress = in_progress;
    }

    public Integer getCompleted() {
        return completed;
    }

    public void setCompleted(Integer completed) {
        this.completed = completed;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "completed = " + completed + "; total = " + total;
    }
}
