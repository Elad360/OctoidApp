package il.ac.shenkar.octoid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


public class ItemListBaseAdapter extends BaseAdapter
{

    private String TAG = "RTT_TaskListAdapter";

    private Context context;
    private LayoutInflater inflater;
    private TaskListModel taskListModel;

    public ItemListBaseAdapter(Context context)
    {
        this.context = context;
        this.taskListModel = TaskListModel.getInstance(context);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        return taskListModel.getCount();
    }

    @Override
    public boolean  isEmpty()
    {
        return taskListModel.isEmpty();
    }

    @Override
    public Task getItem(int i)
    {
        return taskListModel.getItem(i);
    }

    @Override
    public long getItemId(int i)
    {
        return getItem(i).getId();
    }

    @Override
    public boolean hasStableIds ()
    {
        return true;
    }

    public void removeCompletedTasks()
    {
        int i = 0;
        while (i < getCount())
        {
            if (getItem(i).getTaskStatus() == 1)
            {
                taskListModel.removeTask(getItem(i));
                i = 0;  // Check again from the beginning
            }
            else { i++; }
        }

        notifyDataSetChanged();
    }


    private final View.OnClickListener checkBoxOnClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            int position = (Integer) view.getTag();
            Task task = getItem(position);
            if (((CheckBox) view).isChecked()) {  task.setTaskStatus(1);  }
            else  {  task.setTaskStatus(0);  }
            taskListModel.updateTask(task);
            notifyDataSetChanged();
        }

    };

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        TaskRowViewHolder holder;
        Long remainingDueTime = Long.parseLong("0");

        if (convertView==null)
        {
            convertView = this.inflater.inflate(R.layout.item_details_view, null);
            holder = new TaskRowViewHolder();
            holder.taskNameTextView = (TextView) convertView.findViewById(R.id.task_name);
            holder.taskDetailsTextView = (TextView) convertView.findViewById(R.id.task_details);
            holder.taskRowColor = (ImageView) convertView.findViewById(R.id.row_color);
            holder.taskStatusCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox_task);
            holder.taskStatusCheckBox.setOnClickListener(checkBoxOnClickListener);

            convertView.setTag(holder);
        }
        else
        {
            holder = (TaskRowViewHolder) convertView.getTag();
        }

        holder.taskNameTextView.setText(getItem(position).getName());

        holder.taskStatusCheckBox.setTag(position);

        if (getItem(position).getTaskStatus() == 1)     //If task is set to completed
        {
            holder.taskStatusCheckBox.setChecked(true);
            holder.taskRowColor.setBackgroundColor(Color.GREEN);
            holder.taskDetailsTextView.setText("Done");
            holder.taskNameTextView.setPaintFlags(holder.taskNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else    //Task is not completed
        {
            holder.taskStatusCheckBox.setChecked(false);
            holder.taskNameTextView.setPaintFlags(holder.taskNameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

            if (getItem(position).isGeoFence() == 1)    //If Geo fence was set
            {
                holder.taskRowColor.setBackgroundColor(Color.BLUE);
                holder.taskDetailsTextView.setText("Geo fence reminder");
            }
            else if (getItem(position).getDueTime() == 0)    //If no due time was set
            {
                holder.taskRowColor.setBackgroundColor(Color.MAGENTA);
                holder.taskDetailsTextView.setText("No due date");
            }
            else
            {
                remainingDueTime = getItem(position).getDueTime() - System.currentTimeMillis();
                if (remainingDueTime < 0)   //Overdue
                {
                    holder.taskRowColor.setBackgroundColor(Color.RED);
                    holder.taskDetailsTextView.setText("Overdue");
                }
                else    //Show the remaining due time
                {
                    if (remainingDueTime/60000 < 60)
                    {
                        holder.taskDetailsTextView.setText(remainingDueTime / 60000 + " minutes left");
                        holder.taskRowColor.setBackgroundColor(0xffffa500); //Orange color
                    }
                    else if (remainingDueTime/3600000 < 24)
                    {
                        holder.taskDetailsTextView.setText(remainingDueTime / 3600000 + " hours left");
                        holder.taskRowColor.setBackgroundColor(0xffffd700); //Gold color
                    }
                    else
                    {
                        holder.taskDetailsTextView.setText(remainingDueTime / 86400000 + " days left");
                        holder.taskRowColor.setBackgroundColor(0xffffef99); //Bright yellow color
                    }
                }  

            }
        }

        return convertView;
    }

    static class TaskRowViewHolder
    {
        TextView taskNameTextView;
        TextView taskDetailsTextView;
        ImageView taskRowColor;
        CheckBox taskStatusCheckBox;
    }


}
