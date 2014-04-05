package il.ac.shenkar.octoid;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import java.util.Calendar;


public class CreateTaskActivity extends Activity
{
    int mReminder = -1;
    private TaskListModel taskListModel;
    ImageButton btnSelectDate,btnSelectTime, btnSetReminder, btnSetGeoFence;
    Button btnNewTask;
    int mGeoFenceSet = 0;

    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;
    static final int REMINDER_DIALOG_ID = 2;

    // Variables to save user selected date and time
    public  int year,month,day,hour,minute;
    // Declare  the variables to Show/Set the date and time when Time and  Date Picker Dialog first appears
    private int mYear, mMonth, mDay,mHour,mMinute;

    // Constructor
    public CreateTaskActivity()
    {
        // Assign current Date and Time Values to Variables
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        taskListModel = TaskListModel.getInstance(this);
        final TextView descriptionTextView = (TextView) findViewById(R.id.edit_message);

        // get the references of buttons
        btnSelectDate=(ImageButton)findViewById(R.id.button_select_date);
        btnSelectTime=(ImageButton)findViewById(R.id.button_select_time);
        btnSetReminder=(ImageButton)findViewById(R.id.button_set_reminder);
        btnSetGeoFence=(ImageButton)findViewById(R.id.button_set_geo_fence);
        btnNewTask = (Button) findViewById(R.id.button_create_task);


        // Set ClickListener on btnSelectDate
        btnSelectDate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Show the DatePickerDialog
                showDialog(DATE_DIALOG_ID);
            }
        });

        // Set ClickListener on btnSelectTime
        btnSelectTime.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Show the TimePickerDialog
                showDialog(TIME_DIALOG_ID);
            }
        });

        btnSetReminder.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Show the ReminderDialog
                showDialog(REMINDER_DIALOG_ID);
            }
        });

        btnSetGeoFence.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Show the ReminderDialog
                startActivity(new Intent(v.getContext(), GeoFencesActivity.class));
                mGeoFenceSet = 1;
            }
        });



        btnNewTask.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Long dueTime = Long.parseLong("0");
                String taskDescription = descriptionTextView.getText().toString();
                if (taskDescription.isEmpty())
                {
                    Toast.makeText(view.getContext(), "Task description cannot be empty", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if (year != 0 && month != 0 && day != 0 && hour != 0 && minute != 0)
                    {
                        int second = 0;
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day, hour, minute, second);
                        dueTime = calendar.getTimeInMillis();
                        if (mReminder != -1)
                        {
                            switch (mReminder) {
                                case 0:
                                    startAlert(view,taskDescription, dueTime);  //On time
                                    break;
                                case 1:
                                    startAlert(view,taskDescription, dueTime - 15*60000); //15 minutes
                                    break;
                                case 2:
                                    startAlert(view,taskDescription, dueTime - 30*60000); //30 minutes
                                    break;
                                case 3:
                                    startAlert(view,taskDescription, dueTime - 60*60000); //1 hour
                                    break;
                                case 4:
                                    startAlert(view,taskDescription, dueTime - 60*24*60000); //1 day

                            }
                        }
                        else
                        {
                            Toast.makeText(view.getContext(), "Task created. No alarm was set", Toast.LENGTH_LONG).show();
                        }
                    }

                    taskListModel.pushTask(taskDescription,mGeoFenceSet,dueTime);
                    finish();
                }
            }
        });
    }


    // Register  DatePickerDialog listener
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                // the callback received when the user "sets" the Date in the DatePickerDialog
                public void onDateSet(DatePicker view, int yearSelected,
                                      int monthOfYear, int dayOfMonth) {
                    year = yearSelected;
                    month = monthOfYear;
                    day = dayOfMonth;
                    // Set the Selected Date in Select date Button
                   //btnSelectDate.setText("Date selected : "+day+"-"+month+"-"+year);
                    Toast.makeText(view.getContext(), "Date selected : " + day + "-" + month + "-" + year, Toast.LENGTH_LONG).show();
                }
            };

    // Register  TimePickerDialog listener
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                // the callback received when the user "sets" the TimePickerDialog in the dialog
                public void onTimeSet(TimePicker view, int hourOfDay, int min) {
                    hour = hourOfDay;
                    minute = min;
                    // Set the Selected Date in Select date Button
                    //btnSelectTime.setText("Time selected :"+hour+":"+minute);
                    Toast.makeText(view.getContext(), "Time selected :" + hour + ":" + minute, Toast.LENGTH_LONG).show();
                }
            };



    // Method automatically gets Called when you call showDialog()  method
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                // create a new DatePickerDialog with values you want to show
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
            // create a new TimePickerDialog with values you want to show
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this,
                        mTimeSetListener, mHour, mMinute, false);
            case REMINDER_DIALOG_ID:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.set_reminder)
                        .setItems(R.array.reminders_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mReminder = which;
                            }
                        });
                return builder.create();

        }
        return null;
    }


    private void startAlert(View view, String taskDescription, long when)
    {
        Intent intent = new Intent(this, TasksBroadcastReceiver.class);
        intent.putExtra("task", taskDescription);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 234324243, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
        Toast.makeText(this, "Task created. Reminder set to: " + day + "/" + month + "/" + year + " " + hour + ":" + minute, Toast.LENGTH_LONG).show();
    }

}


