package il.ac.shenkar.octoid;


public class Task
{
    private String name;
    private Long id;
    private int isDone;
    private int isGeoFenceSet;
    private Long dueTimeValue;

    public Task(Long id, String name, int isDone, int isGeoFenceSet, Long dueTimeValue)
    {
        this.name = name;
        this.id = id;
        this.isDone = isDone;
        this.isGeoFenceSet = isGeoFenceSet;
        this.dueTimeValue = dueTimeValue;
    }

    public String getName()
    {
        return name;
    }

    public String toString() { return getName();  }

    public Long getId(){
        return id;
    }

    public int getTaskStatus() { return isDone; }
    public void setTaskStatus(int isDone)
    {
        this.isDone = isDone;
    }

    public int isGeoFence() { return isGeoFenceSet; }

    public Long getDueTime() { return dueTimeValue; }
    public void setDueTime(Long dueTime)
    {
        this.dueTimeValue = dueTime;
    }

}