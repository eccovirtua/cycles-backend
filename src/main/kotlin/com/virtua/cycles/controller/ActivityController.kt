//package com.virtua.cycles.controller
//
//import com.virtua.cycles.model.Activity
//import com.virtua.cycles.repository.ActivityRepository
//import org.springframework.web.bind.annotation.*
//
//@RestController
//@RequestMapping("/activities")
//class ActivityController(private val repository: ActivityRepository) {
//
//    @PostMapping
//    fun saveActivity(@RequestBody activity: Activity): Activity {
//        return repository.save(activity)
//    }
//
//    @GetMapping
//    fun getAllActivities(): List<Activity> {
//        return repository.findAll()
//    }
//}
