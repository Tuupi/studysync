package com.lunix.studysync;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "TaskFragment";
    private String mParam1;
    private String mParam2;
    private DatabaseReference databaseUsers;
    final Calendar k = Calendar.getInstance();

    RecyclerView recyclerView;
    ArrayList<Task> list;
    DatabaseReference databaseReference;
    TaskAdapter taskAdapter;

    FirebaseAuth mAuth;
    String user;
    EditText selectedDate;
    Button pickDateBtn;
    public TaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TaskFragment.
     */
    public static TaskFragment newInstance(String param1, String param2) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        k.set(Calendar.DAY_OF_MONTH, k.get(Calendar.DAY_OF_MONTH));
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

//        recyclerView = findViewById(R.id.recycleview);
//        databaseReference = FirebaseDatabase.getInstance().getReference("users");
//        list = new ArrayList<>();
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        taskAdapter = new TaskAdapter(this, list);
//        recyclerView.setAdapter(taskAdapter);
//
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
//                    Mytask mytask = dataSnapshot.getValue(Mytask.class);
//                    list.add(mytask);
//                }
//                taskAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task, container, false);
        recyclerView = rootView.findViewById(R.id.listTask);
        Log.d(TAG, "Debug log message" + user);
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user).child("task");
        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(requireContext(), list);
        recyclerView.setAdapter(taskAdapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    CheckDate check = new CheckDate(dataSnapshot.getValue(Task.class).getDate(), k);
                    if(check.compareDates() == "after") {
                        Log.d(TAG, "Test nama : " + dataSnapshot.getValue(Task.class).getName());
                        Log.d(TAG, "Test nama : " + dataSnapshot.getValue(Task.class).getCourse());
                        Log.d(TAG, "Test nama : " + dataSnapshot.getValue(Task.class).getDate());
                        Task task = dataSnapshot.getValue(Task.class);
                        list.add(task);
                    } else if (check.compareDates() == "before") {
                        Task task = dataSnapshot.getValue(Task.class);
                        databaseReference.child(task.getName()).setValue(null);
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.RIGHT) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    int position = viewHolder.getAdapterPosition(); // this is how you can get the position
                    Task task = taskAdapter.list.get(position);
                    builder.setMessage("Are you sure you want to delete " + task.getName());
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //if user pressed "yes", then he is allowed to exit from application
                            // You will have your own class ofcourse.

                            // then you can delete the object
                            databaseReference.child(task.getName()).setValue(null);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //if user select "No", just cancel this dialog and continue with app
                            taskAdapter.notifyItemChanged(position);
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();

                }
                if (direction == ItemTouchHelper.LEFT) {
                Log.d(TAG, "Test Left");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    int position = viewHolder.getAdapterPosition(); // this is how you can get the position

                    builder.setMessage("Edit This? ");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            taskAdapter.notifyItemChanged(position);
                            editTask(taskAdapter.list.get(position));
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //if user select "No", just cancel this dialog and continue with app
                            taskAdapter.notifyItemChanged(position);
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();


                }
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

        return rootView;
    }
    private void editTask(Task t){
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.createexam);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);
        pickDateBtn = dialog.findViewById(R.id.idBtnPickDate);
        selectedDate = dialog.findViewById(R.id.Date);
        EditText name = dialog.findViewById(R.id.examName);
        EditText course = dialog.findViewById(R.id.CourseName);
        EditText date = dialog.findViewById(R.id.Date);
        Button submit = dialog.findViewById(R.id.createExam);
        Task task = t;
        String placeholder = task.getName();
        name.setText(task.getName());
        course.setText(task.getCourse());
        date.setText(task.getDate());
        dialog.show();
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child(placeholder).setValue(null);
                task.setName(name.getText().toString());
                task.setCourse(course.getText().toString());
                task.setDate(date.getText().toString());
                databaseReference.child(name.getText().toString()).setValue(task);
                Toast.makeText(getContext(),"Updated " + name.getText(),Toast.LENGTH_SHORT).show();
                dialog.dismiss();
////                ExamModel exam = new ExamModel(name.getText().toString(), course.getText().toString(), date.getText().toString());
//                mDatabase.child("users").child(userid).child("exams").child(name.getText().toString()).child("course").setValue(course.getText().toString());
//                mDatabase.child("users").child(userid).child("exams").child(name.getText().toString()).child("date").setValue(date.getText().toString());
            }
        });
        datebutton();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
    }
    private void datebutton(){

        // on below line we are adding click listener for our pick date button
        pickDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting
                // the instance of our calendar.
                final Calendar c = Calendar.getInstance();

                // on below line we are getting
                // our day, month and year.
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                c.set(Calendar.DAY_OF_MONTH, day);
                // on below line we are creating a variable for date picker dialog.
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        // on below line we are passing context.
                        getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our text view.
                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.MONTH, monthOfYear);
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                cal.set(Calendar.YEAR, year);
                                if(cal.before(c)) {
                                    Toast.makeText(getContext(),"please enter a valid date",Toast.LENGTH_SHORT).show();
                                    // notify user about wrong date.
                                    return;
                                }
                                StringBuilder date = new StringBuilder();
                                date.append((dayOfMonth<10?"0":"")).append(dayOfMonth)
                                        .append("-").append((monthOfYear + 1) < 10 ? "0" : "")
                                        .append((monthOfYear+1)).append("-").append(year);
                                selectedDate.setText(date);
                            }
                        },
                        // on below line we are passing year,
                        // month and day for selected date in our date picker.
                        year, month, day);

                // at last we are calling show to
                // display our date picker dialog.
                datePickerDialog.show();
            }
        });
    }

}