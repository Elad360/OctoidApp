package il.ac.shenkar.octoid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

public class ListViewTasksActivity extends Activity
{

    Context context;
    ItemListBaseAdapter taskListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        TaskListModel taskListModel = TaskListModel.getInstance(this);

        ListView taskList = (ListView) findViewById(R.id.listV_main);
        taskListAdapter = new ItemListBaseAdapter(this);
        taskList.setAdapter(taskListAdapter);

        ImageButton newTaskButton = (ImageButton) findViewById(R.id.new_task_button);
        newTaskButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(context, CreateTaskActivity.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        super.onOptionsItemSelected(item);

        switch(item.getItemId())
        {
            case R.id.action_remove_tasks:
                Toast.makeText(getBaseContext(), "Removing tasks", Toast.LENGTH_SHORT).show();
                taskListAdapter.removeCompletedTasks();
                break;
        }
        return true;

    }

    @Override
    public void onStart()
    {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        taskListAdapter.notifyDataSetChanged();
    }
}
