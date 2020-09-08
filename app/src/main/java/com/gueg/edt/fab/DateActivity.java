package com.gueg.edt.fab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.transition.ArcMotion;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gueg.edt.R;
import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Locale;

public class DateActivity extends AppCompatActivity {

    private ViewGroup container;
    CalendarView calendarView;
    LocalDate selectedDate;
    ArrayList<DayCellContainer> cells = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date);

        container = findViewById(R.id.activity_date_container);
        calendarView = findViewById(R.id.activity_date_picker);
        calendarView.setDayBinder(new DayBinder<DayCellContainer>() {
            @NotNull
            @Override
            public DayCellContainer create(@NotNull View view) {
                DayCellContainer dcc = new DayCellContainer(view);
                cells.add(dcc);
                return dcc;
            }

            @Override
            public void bind(@NotNull DayCellContainer viewContainer, @NotNull CalendarDay calendarDay) {
                viewContainer.date = calendarDay;
                viewContainer.textview.setText(Integer.toString(calendarDay.getDate().getDayOfMonth()));
            }
        });
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(10);
        YearMonth lastMonth = currentMonth.plusMonths(10);
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);

        setupSharedEelementTransitions1();

        View.OnClickListener dismissListener = view -> dismiss();
        container.setOnClickListener(dismissListener);
        container.findViewById(R.id.activity_date_close).setOnClickListener(dismissListener);
        container.findViewById(R.id.activity_date_ok).setOnClickListener(v -> {
            Intent data = new Intent();
            //data.putExtra(calendarView.get)
            //setResult(RESULT_OK, );
            dismiss();
        });
    }

    class DayCellContainer extends ViewContainer {

        CalendarDay date;
        TextView textview;

        DayCellContainer(View view) {
            super(view);
            textview = view.findViewById(R.id.calendar_day_picker_text);

            view.setOnClickListener(v -> {
                selectedDate = date.getDate();

                for(DayCellContainer dcc : cells) {
                    if(isSameWeek(dcc.date.getDate(), selectedDate)) {
                        dcc.textview.setTextColor(0xffffffff);
                        if(dcc.date.getDate().getDayOfWeek() == DayOfWeek.MONDAY)
                            dcc.textview.setBackgroundResource(R.drawable.date_selected_start);
                        else if(dcc.date.getDate().getDayOfWeek() == DayOfWeek.SUNDAY)
                            dcc.textview.setBackgroundResource(R.drawable.date_selected_end);
                        else
                            dcc.textview.setBackgroundResource(R.drawable.date_selected_middle);
                    } else {
                        dcc.textview.setTextColor(0xff000000);
                        dcc.textview.setBackground(null);
                    }
                }
            });
        }
    }

    boolean isSameWeek(LocalDate date1, LocalDate date2) {
        return getWeekNumber(date1) == getWeekNumber(date2);
    }

    int getWeekNumber(LocalDate date) {
        return date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
    }


    public void setupSharedEelementTransitions1() {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(50f);
        arcMotion.setMinimumVerticalAngle(50f);

        Interpolator easeInOut = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in);

        MorphFabToDialog sharedEnter = new MorphFabToDialog();
        sharedEnter.setPathMotion(arcMotion);
        sharedEnter.setInterpolator(easeInOut);

        MorphDialogToFab sharedReturn = new MorphDialogToFab();
        sharedReturn.setPathMotion(arcMotion);
        sharedReturn.setInterpolator(easeInOut);

        if (container != null) {
            sharedEnter.addTarget(container);
            sharedReturn.addTarget(container);
        }
        getWindow().setSharedElementEnterTransition(sharedEnter);
        getWindow().setSharedElementReturnTransition(sharedReturn);
    }


    public void setupSharedEelementTransitions2() {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(50f);
        arcMotion.setMinimumVerticalAngle(50f);

        Interpolator easeInOut = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in);

        MorphTransition sharedEnter = new MorphTransition(getResources().getColor(R.color.fab_background_color),
                getResources().getColor(R.color.dialog_background_color), 100, getResources().getDimensionPixelSize(R.dimen.dialog_corners), true);
        sharedEnter.setPathMotion(arcMotion);
        sharedEnter.setInterpolator(easeInOut);

        MorphTransition sharedReturn = new MorphTransition(getResources().getColor( R.color.dialog_background_color),
                getResources().getColor(R.color.fab_background_color), getResources().getDimensionPixelSize(R.dimen.dialog_corners), 100,  false);
        sharedReturn.setPathMotion(arcMotion);
        sharedReturn.setInterpolator(easeInOut);

        if (container != null) {
            sharedEnter.addTarget(container);
            sharedReturn.addTarget(container);
        }
        getWindow().setSharedElementEnterTransition(sharedEnter);
        getWindow().setSharedElementReturnTransition(sharedReturn);
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    public void dismiss() {
        setResult(Activity.RESULT_CANCELED);
        finishAfterTransition();
    }

}
