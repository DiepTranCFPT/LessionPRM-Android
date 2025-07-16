package com.lessionprm.ui.courses;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lessionprm.R;
import com.lessionprm.data.model.Course;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courses;
    private OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public CourseAdapter(List<Course> courses, OnCourseClickListener listener) {
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCourseImage;
        private TextView tvCourseTitle, tvCourseInstructor, tvCourseDuration;
        private TextView tvCoursePrice, tvCourseDescription;
        private TextView tvCourseLevel, tvCourseCategory, tvStudentCount;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivCourseImage = itemView.findViewById(R.id.iv_course_image);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
            tvCourseInstructor = itemView.findViewById(R.id.tv_course_instructor);
            tvCourseDuration = itemView.findViewById(R.id.tv_course_duration);
            tvCoursePrice = itemView.findViewById(R.id.tv_course_price);
            tvCourseDescription = itemView.findViewById(R.id.tv_course_description);
            tvCourseLevel = itemView.findViewById(R.id.tv_course_level);
            tvCourseCategory = itemView.findViewById(R.id.tv_course_category);
            tvStudentCount = itemView.findViewById(R.id.tv_student_count);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCourseClick(courses.get(position));
                    }
                }
            });
        }

        public void bind(Course course) {
            // Set course title
            tvCourseTitle.setText(course.getTitle());
            
            // Set instructor name
            if (course.getInstructorName() != null) {
                tvCourseInstructor.setText(course.getInstructorName());
                tvCourseInstructor.setVisibility(View.VISIBLE);
            } else {
                tvCourseInstructor.setVisibility(View.GONE);
            }
            
            // Set duration
            if (course.getDurationHours() != null) {
                tvCourseDuration.setText(course.getDurationHours() + " giờ");
                tvCourseDuration.setVisibility(View.VISIBLE);
            } else {
                tvCourseDuration.setVisibility(View.GONE);
            }
            
            // Set price
            tvCoursePrice.setText(course.getFormattedPrice());
            
            // Set description
            if (course.getShortDescription() != null && !course.getShortDescription().isEmpty()) {
                tvCourseDescription.setText(course.getShortDescription());
                tvCourseDescription.setVisibility(View.VISIBLE);
            } else if (course.getDescription() != null && !course.getDescription().isEmpty()) {
                tvCourseDescription.setText(course.getDescription());
                tvCourseDescription.setVisibility(View.VISIBLE);
            } else {
                tvCourseDescription.setVisibility(View.GONE);
            }
            
            // Set level
            if (course.getLevel() != null) {
                tvCourseLevel.setText(course.getLevel());
                tvCourseLevel.setVisibility(View.VISIBLE);
            } else {
                tvCourseLevel.setVisibility(View.GONE);
            }
            
            // Set category
            if (course.getCategory() != null) {
                tvCourseCategory.setText(course.getCategory());
                tvCourseCategory.setVisibility(View.VISIBLE);
            } else {
                tvCourseCategory.setVisibility(View.GONE);
            }
            
            // Set student count
            if (course.getStudentCount() != null) {
                tvStudentCount.setText(course.getStudentCount() + " học viên");
                tvStudentCount.setVisibility(View.VISIBLE);
            } else {
                tvStudentCount.setVisibility(View.GONE);
            }
            
            // Load course image
            if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(course.getImageUrl())
                        .placeholder(R.drawable.ic_course_placeholder)
                        .error(R.drawable.ic_course_placeholder)
                        .into(ivCourseImage);
            } else {
                ivCourseImage.setImageResource(R.drawable.ic_course_placeholder);
            }
        }
    }
}