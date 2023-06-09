package Misak.CS.ICES4HU.Semester.Service.impl;

import lombok.AllArgsConstructor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import Misak.CS.ICES4HU.Semester.Repository.SemesterRepository;
import Misak.CS.ICES4HU.Semester.Service.SemesterService;
import Misak.CS.ICES4HU.Course.Entity.CourseEntity;
import Misak.CS.ICES4HU.Course.Repository.CourseRepository;
import Misak.CS.ICES4HU.Semester.Entity.SemesterEntity;



@Service
@AllArgsConstructor
public class SemesterServiceImpl implements SemesterService{
    SemesterRepository semesterRepository;
    CourseRepository courseRepository;
    

    public void createSemester(String startDate, String endDate, String description){
        SemesterEntity semesterEntity = new SemesterEntity();
        Date startDate1 = Date.valueOf(startDate);
        Date endDate1 = Date.valueOf(endDate);

        // check all semesters if new entities date is collapse with start and end date of any semester entity in database 
        // if it is collapse throw exception
        // else save new entity
        for (SemesterEntity semester : semesterRepository.findAll()) {
            // convert string to date
            Date startDate2 = Date.valueOf(semester.getStartDate());
            Date endDate2 = Date.valueOf(semester.getEndDate());

            // check if new entity start date is between any start and end date of any semester entity in database
            if (startDate1.after(startDate2) && startDate1.before(endDate2)) {
                throw new IllegalArgumentException("Start date is between another semester start and end date");
            }

            // check if new entity end date is between any start and end date of any semester entity in database
            if (endDate1.after(startDate2) && endDate1.before(endDate2)) {
                throw new IllegalArgumentException("End date is between another semester start and end date");
            }

            


            


            
        }
        // check if new entity end date is before current date
        if ( endDate1.before(new Date(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("Start date and end date is before current date");
        }

        // check if new entity start date is after end date
        if (startDate1.after(endDate1)) {
            throw new IllegalArgumentException("Start date is after end date");
        }

        // create new entity
        semesterEntity.setStartDate(startDate);
        semesterEntity.setEndDate(endDate);
        semesterEntity.setDescription(description);
        semesterRepository.save(semesterEntity);
    }


    public SemesterEntity getActiveSemester(){
        // get current date
        Date currentDate = new Date(System.currentTimeMillis());
        // check all semesters if current date is between start and end date of any semester entity in database 
        // if it is return that entity
        // else throw exception
        for (SemesterEntity semester : semesterRepository.findAll()) {
            // convert string to date
            Date startDate = Date.valueOf(semester.getStartDate());
            Date endDate = Date.valueOf(semester.getEndDate());

            // check if current date is between any start and end date of any semester entity in database
            if (currentDate.after(startDate) && currentDate.before(endDate)) {
                return semester;
            }
        }
        throw new IllegalArgumentException("There is no active semester");
    }

    public List<SemesterEntity> getAllSemesters(){
        return semesterRepository.findAll();
    }

    public SemesterEntity getSemesterById(Long id){
        return semesterRepository.findSemesterEntityById(id);
    }
    
    public void addSemesterCourses(Long id, List<CourseEntity> courseList){
        SemesterEntity semesterEntity = semesterRepository.findSemesterEntityById(id);
        List<CourseEntity> courseList1 = semesterEntity.getCourseList();
        for (CourseEntity course : courseList) {
            if( course.getId() != null && courseRepository.findCourseEntityById(course.getId()) != null){
                System.out.println("course already exist");
                continue;
            }
            CourseEntity courseEntity = new CourseEntity();
        
            courseEntity.setName(course.getName());
            courseEntity.setCode(course.getCode());
            courseEntity.setCredit(course.getCredit());
            courseEntity.setDepartment(course.getDepartment());
            courseRepository.save(courseEntity);

            courseList1.add(courseEntity);
        }
        semesterEntity.setCourseList(courseList1);
        semesterRepository.save(semesterEntity);
    }

    public List<CourseEntity> getCoursesByIds(List<Long> ids){
        return courseRepository.findAllById(ids);
    }

    public SemesterEntity deleteSemesterCourseList(Long id, Long courseId){
        SemesterEntity semesterEntity = semesterRepository.findSemesterEntityById(id);
        CourseEntity courseEntity = courseRepository.findCourseEntityById(courseId);
        
        for (CourseEntity course : semesterEntity.getCourseList()) {
            System.out.println("course id: " + courseId + " course id in list: " + course.getId());
            if (course.getId() == courseId) {
                semesterEntity.getCourseList().remove(courseEntity);
                semesterRepository.save(semesterEntity);
                return semesterEntity;
            }
        }

        throw new IllegalArgumentException("Course id is not in semester course list");
    }

    public SemesterEntity updateSemesterCourse(Long id, CourseEntity updatedCourseEntity){
        SemesterEntity semesterEntity = semesterRepository.findSemesterEntityById(id);
        CourseEntity courseEntity = courseRepository.findCourseEntityById(updatedCourseEntity.getId());
        courseEntity.setName(updatedCourseEntity.getName());
        courseEntity.setCode(updatedCourseEntity.getCode());
        courseEntity.setCredit(updatedCourseEntity.getCredit());
        courseEntity.setInstructor(updatedCourseEntity.getInstructor());
        courseEntity.setDepartment(updatedCourseEntity.getDepartment());
        
        courseRepository.save(courseEntity);
        return semesterEntity;
    }


    public SemesterEntity instructorUpdateSemesterCourse(CourseEntity updatedCourseEntity){
        SemesterEntity semesterEntity = getActiveSemester();
        CourseEntity courseEntity = courseRepository.findCourseEntityById(updatedCourseEntity.getId());
        courseEntity.setInstructor(updatedCourseEntity.getInstructor());

        
        courseRepository.save(courseEntity);
        return semesterEntity;
    }

    public List<CourseEntity> getCoursesByManagerName(String managerName){
        // get active semester
        SemesterEntity semesterEntity = getActiveSemester();

        // get all courses in active semester
        List<CourseEntity> courseList = semesterEntity.getCourseList();

        // create new list to store courses that manager is instructor of them
        List<CourseEntity> managerCourseList = new ArrayList<CourseEntity>();

        // check if manager is instructor of any course in active semester
        for (CourseEntity course : courseList) {
            if (course.getDepartment().equals(managerName)) {
                managerCourseList.add(course);
            }
        }

        
        return managerCourseList;
    }

    public List<CourseEntity> getCoursesByInstructorName(String instructorName){
        SemesterEntity semesterEntity = getActiveSemester();
        List<CourseEntity> courseList = semesterEntity.getCourseList();

         // create new list to store courses that manager is instructor of them
         List<CourseEntity> instructorCourseList = new ArrayList<CourseEntity>();

         // check if manager is instructor of any course in active semester
         for (CourseEntity course : courseList) {
             if (course.getInstructor()!= null && course.getInstructor().equals(instructorName)) {
                instructorCourseList.add(course);
             }
         }
 
         
         return instructorCourseList;
    }
}
