package ru.mtuci.coursemanagement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mtuci.coursemanagement.model.Student;
import ru.mtuci.coursemanagement.repository.StudentRepository;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StudentController {
    private final StudentRepository repo;

    @GetMapping("/students")
    public String studentsPage(Model model) {
        model.addAttribute("students", repo.findAll());
        model.addAttribute("student", new Student());
        return "students";
    }

    @PostMapping("/students")
    public String createStudent(@ModelAttribute Student st) {
        repo.save(st);
        return "redirect:/students";
    }

    @GetMapping("/api/students")
    @ResponseBody
    public List<Student> all() {
        return repo.findAll();
    }

    @GetMapping("/api/students/{id}")
    @ResponseBody
    public ResponseEntity<Student> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/students/{id}")
    @ResponseBody
    public ResponseEntity<Student> update(@PathVariable Long id, @RequestBody Student payload) {
        return repo.findById(id).map(st -> {
            st.setName(payload.getName());
            st.setEmail(payload.getEmail());
            st.setUserId(payload.getUserId());
            return ResponseEntity.ok(repo.save(st));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/students/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
