package com.campusdual.fundme.webcontroller;

import com.campusdual.fundme.model.Comment;
import com.campusdual.fundme.model.Donation;
import com.campusdual.fundme.model.Project;
import com.campusdual.fundme.model.User;

import com.campusdual.fundme.model.dto.*;

import com.campusdual.fundme.model.dto.dtopmapper.CommentMapper;
import com.campusdual.fundme.model.dto.dtopmapper.DonationMapper;
import com.campusdual.fundme.model.dto.dtopmapper.ProjectMapper;
import com.campusdual.fundme.model.dto.dtopmapper.UserMapper;

import com.campusdual.fundme.api.IDonationService;
import com.campusdual.fundme.api.IProjectService;
import com.campusdual.fundme.api.IUserService;

import com.campusdual.fundme.model.repository.UserRepository;
import com.campusdual.fundme.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/fundme/controller/web")
public class WebController {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private AreaService areaService;
    @Autowired
    private IUserService userService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private IProjectService projectService;

    @Autowired
    private IDonationService donationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String showLoginForm() { return "login";  }

    @PostMapping("/authentication")
    public String authenticate(@RequestParam String username, @RequestParam String password) {

        boolean isAuthenticated = loginService.authenticate(username, password);

        if (isAuthenticated) {

            return "redirect:/fundme/controller/web/dashboard";

        } else {

            return "redirect:/fundme/controller/web/login"; // return "redirect:/api/public/showLoginForm?error=true";

        }

    }

    @GetMapping("/register")
    public String showRegisterForm (Model model) {

        List<CountryDTO> countries = countryService.getAllCountries();
        model.addAttribute("countries", countries);
        model.addAttribute("user", new UserDTO());

        return "register";

    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("user") UserDTO userDTO) {

        userService.insertUser(userDTO);

        return "redirect:/fundme/controller/web/login";

    }

    @GetMapping(value = "/dashboard")
    public String dashboard(Model model) {

        List<Project> topProjectsList = projectService.getTopProjects();
        model.addAttribute("topProjectsList", topProjectsList);

        List<Donation> topDonationsList = donationService.getTopDonations();
        model.addAttribute("topDonationsList", topDonationsList);

        return "dashboard";

    }

    @GetMapping(value = "/createProject")
    public String createProject (Model model) {

        List<AreaDTO> areas = areaService.getAllAreas();
        model.addAttribute("areas", areas);
        model.addAttribute("project", new ProjectDTO());

        return "create-project";

    }

    @PostMapping("/createProject")
    public String createProject (@ModelAttribute("project") ProjectDTO projectDTO) {

        Project project = ProjectMapper.INSTANCE.toEntity(projectDTO);

        UserDTO authenticatedUser = userService.getUser(new UserDTO());
        User user = UserMapper.INSTANCE.toEntity(authenticatedUser);

        project.setUserId(user);
        project.setDateAdded(new Date());
        int totalAmount = 0;
        project.setTotalAmount(totalAmount);

        projectService.insertProject(ProjectMapper.INSTANCE.toDTO(project));

        return "redirect:/fundme/controller/web/projects";

    }

    @GetMapping(value = "/viewProject/{projectId}")
    public String viewProject(@PathVariable int projectId, Model model) {

        ProjectDTO projectDTO = projectService.getProjectById(projectId);
        model.addAttribute("projectDetails", projectDTO);

        List<Comment> commentList = commentService.getCommentsByProjectId(projectDTO);
        model.addAttribute("commentList", commentList);

        return "view-project";

    }

    @GetMapping("/donate/{projectId}")
    public String donateToProject (@PathVariable("projectId") int projectId, Model model) {

        ProjectDTO projectDTO = projectService.getProjectById(projectId);
        model.addAttribute("project", projectDTO);

        return "donate-project";

    }

    @PostMapping("/donate/{projectId}")
    public String donateToProject(@PathVariable("projectId") int projectId, @RequestParam("amount") int amount) {

        ProjectDTO projectDTO = projectService.getProjectById(projectId);
        UserDTO authenticatedUser = userService.getUser(new UserDTO());

        Project project = ProjectMapper.INSTANCE.toEntity(projectDTO);
        User user = UserMapper.INSTANCE.toEntity(authenticatedUser);

        Donation donation = new Donation();

        donation.setUserId(user);
        donation.setProjectId(project);
        donation.setAmount(amount);
        donation.setDateAdded(new Date());

        donationService.insertDonation(DonationMapper.INSTANCE.toDTO(donation));

        project.setTotalAmount(project.getTotalAmount() + amount);
        projectService.updateProject(ProjectMapper.INSTANCE.toDTO(project));

        return "redirect:/fundme/controller/web/donations";

    }

    @GetMapping(value = "/comment/{projectId}")
    public String commentProject (@PathVariable("projectId") int projectId, Model model) {

        ProjectDTO projectDTO = projectService.getProjectById(projectId);
        model.addAttribute("project", projectDTO);

        model.addAttribute("comment", new CommentDTO());

        return "comment-project";

    }

    @PostMapping("/comment/{projectId}")
    public String commentProject(@PathVariable("projectId") int projectId, @ModelAttribute("comment") CommentDTO commentDTO) {

        Comment comment = CommentMapper.INSTANCE.toEntity(commentDTO);

        ProjectDTO projectDTO = projectService.getProjectById(projectId);
        Project project = ProjectMapper.INSTANCE.toEntity(projectDTO);

        UserDTO authenticatedUser = userService.getUser(new UserDTO());
        User user = UserMapper.INSTANCE.toEntity(authenticatedUser);

        comment.setUserId(user);
        comment.setProjectId(project);
        comment.setDateAdded(new Date());

        commentService.insertComment(CommentMapper.INSTANCE.toDTO(comment));

        return "redirect:/fundme/controller/web/viewProject/{projectId}";

    }

    @GetMapping(value = "/projects")
    public String allProjects(Model model) {

        List<ProjectDTO> projectList = projectService.getAllProjects();
        model.addAttribute("projectList", projectList);

        return "projects";
    }

    @GetMapping(value = "/myProjects")
    public String myProjects(Model model) {

        List<Project> myProjects = projectService.getProjectsByAuthenticatedUser();
        model.addAttribute("myProjects", myProjects);

        return "my-projects";

    }

    @GetMapping(value = "/donations")
    public String donations(Model model) {

        List<Donation> donationList = donationService.getAllDonationsOrderByDateAddedDesc();
        model.addAttribute("donationList", donationList);

        return "donations";

    }

    @GetMapping(value = "/myDonations")
    public String myDonations(Model model) {

        List<Donation> myDonations = donationService.getDonationsByAuthenticatedUser();
        model.addAttribute("myDonations", myDonations);

        return "my-donations";

    }

    @GetMapping(value = "/userProfile")
    public String userProfile(Model model) {

        UserDTO authenticatedUser = userService.getUser(new UserDTO());
        model.addAttribute("authenticatedUser", authenticatedUser);

        return "user-profile";

    }

    @GetMapping("/accessDenied")
    public String customErrorPage() { return "error"; }

    @GetMapping(value = "/logout")
    public String logout (HttpServletRequest request) {

        request.getSession().invalidate();

        return "redirect:/fundme/controller/web/login";

    }

    @GetMapping("/deleteAccount")
    public String deleteAccount() {
        return "delete-account";
    }
    @GetMapping("/notifications")
    public String notifications() {
        return "notifications";
    }

    @GetMapping("/admin")
    @ResponseBody
    public String dashboardAdmin() {
        return "Welcome to the administration area";
    }

    @GetMapping(value = "/settings")
    @ResponseBody
    public String settings() { return "Wellcome to settings"; }

    @GetMapping(value = "/editProfile")
    public String editProfile() { return "edit-profile"; }





}