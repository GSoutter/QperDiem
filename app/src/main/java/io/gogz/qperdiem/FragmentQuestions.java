package io.gogz.qperdiem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import io.gogz.qperdiem.adapters.QuestionListAdapter;
import io.gogz.qperdiem.room_db.ContextQ;
import io.gogz.qperdiem.room_db.Question;
import io.gogz.qperdiem.room_db.QuestionContextCrossRef;
import io.gogz.qperdiem.room_db.QuestionContextCrossRefDao;
import io.gogz.qperdiem.viewmodels.QuestionViewModel;

import static android.app.Activity.RESULT_OK;
import static java.lang.Long.parseLong;

public class FragmentQuestions extends Fragment implements QuestionListAdapter.OnQuestionListener{


    private static final String TAG = "FragmentQuestions";
    private QuestionViewModel mQuestionViewModel;
    public static final int NEW_QUESTION_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_QUESTION_ACTIVITY_REQUEST_CODE = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questions, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        final QuestionListAdapter adapter = new QuestionListAdapter(getActivity().getBaseContext(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getBaseContext()));

        mQuestionViewModel = new ViewModelProvider(this).get(QuestionViewModel.class);

        mQuestionViewModel.getQuestions().observe(getViewLifecycleOwner(), new Observer<List<Question>>() {
            @Override
            public void onChanged(@Nullable final List<Question> questions) {
                // Update the cached copy of the questions in the adapter.
                adapter.setQuestions(questions);
            }
        });

        // Floating Action Button coding
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getBaseContext(), NewQuestionActivity.class);
                startActivityForResult(intent, NEW_QUESTION_ACTIVITY_REQUEST_CODE);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });



        return view;

    }

//
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_QUESTION_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Question question = new Question();
            question.text = data.getStringExtra("questionText");
            long questionId = data.getLongExtra("questionId", 0);
            question.questionId = questionId;

            String contextOnString = data.getStringExtra("context_on");
            String contextOffString = data.getStringExtra("context_off");


            assert contextOnString != null;
            String[] contextOnIdArray = contextOnString.split(" ");
            assert contextOffString != null;
            String[] contextOffIdArray = contextOffString.split(" ");


            if (data.getBooleanExtra("delete_toggle", false)){
                mQuestionViewModel.deleteOne(question);

            }else{
                mQuestionViewModel.insert(question);

                if (!contextOnIdArray[0].equals("")){
                    for (String contextId : contextOnIdArray){
                        mQuestionViewModel.insertOneQuestionContextCrossRef(new QuestionContextCrossRef(questionId, parseLong(contextId)));
                    }
                }
                if (!contextOffIdArray[0].equals("")) {
                    for (String contextId : contextOffIdArray) {
                        mQuestionViewModel.deleteOneQuestionContextCrossRef(new QuestionContextCrossRef(questionId, parseLong(contextId)));
                    }
                }

            }
        }else if (requestCode == NEW_QUESTION_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Question question = new Question();
            question.text = data.getStringExtra(NewQuestionActivity.EXTRA_REPLY);
            mQuestionViewModel.insert(question);
        } else {
            Toast.makeText(
                    getActivity().getBaseContext(),
                    R.string.empty_not_saved,
                    Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onQuestionClick(int position) {
//        To get access to the question selected do below. I think this is not necessary as I can use the posistion
        Log.d(TAG, "onQuestionClick: clicked");

        Intent intent = new Intent(getActivity().getBaseContext(), EditQuestionActivity.class);

        // TODO: work out a way to avoid making an additional call to db.
        intent.putExtra("questionId", mQuestionViewModel.getQuestions().getValue().get(position).questionId);
        startActivityForResult(intent, EDIT_QUESTION_ACTIVITY_REQUEST_CODE);

    }
}
