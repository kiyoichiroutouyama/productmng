package com.example.productmng.Controller;


import com.example.productmng.LoginForm;
import com.example.productmng.Service.PgProductmngService;
import com.example.productmng.Service.PgcategoryService;
import com.example.productmng.entity.*;
import com.example.productmng.productmngAdd;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class productmngController {
    @Autowired
    private PgProductmngService pgProductmngService;
    @Autowired
    private PgcategoryService pgcategoryService;
    @Autowired
    private HttpSession session;


    @GetMapping("/login")
    public String index(@ModelAttribute("loginIds") LoginForm loginForm) {
        return "login";
    }

    @PostMapping("login")
    public String login2(@Validated @ModelAttribute("loginIds") LoginForm loginIds, BindingResult bindingResult, Model model) {
        var resultlogin = pgProductmngService.findById(loginIds.getLoginId(), loginIds.getPassword());
        session.setAttribute("user", resultlogin);
        if (bindingResult.hasErrors()) {
            return "login";
        }
        if (resultlogin != null) {
            return "redirect:/menu";
        } else {
            model.addAttribute("error", "IDまたはパスワードが不正です");
            return "login";
        }
    }

    @PostMapping("/logout.html")
    public String logout(@ModelAttribute("loginIds") LoginForm loginForm) {
        session.invalidate();
        return "logout.html";
    }

    @GetMapping("/menu")
    public String productName(@RequestParam(name = "keyword", defaultValue = "") String keyword, Model model) {
        loginpassRecord loginpass = (loginpassRecord)session.getAttribute("user");
        if (loginpass == null){
            return "redirect:/login";
        }
        if (keyword.isEmpty()) {
            model.addAttribute("productslist", pgProductmngService.findAll());
        } else {
            model.addAttribute("productslist", pgProductmngService.findByName(keyword));
        }
        model.addAttribute("category", pgcategoryService.findAll());

        return "menu";
    }

    @GetMapping("/insert")
    public String insertP1(@ModelAttribute("productmngupdate") productmngAdd productmngadd) {
        return "insert";
    }

        @PostMapping("/insert")
        public String insertP2(@Validated @ModelAttribute("productmngupdate") productmngAdd productmngadd,  BindingResult bindingResult, Model model) {
            var result2 = pgProductmngService.findByRecord(productmngadd.getProductId());
            if (bindingResult.hasErrors()) {
                return "insert";
            }else if (result2 == null ){
                pgProductmngService.insert(new insertRecord(productmngadd.getProductId(), productmngadd.getProductName(), productmngadd.getProductPrice(), productmngadd.getCategoryId(), productmngadd.getDescription()));
                model.addAttribute("complete", "登録に成功しました");
                return "success";
            }else {
                model.addAttribute("errorsyori", "商品コードが重複しています");
                return "insert";
            }
        }

    @GetMapping("/detail/{productId}")
    public String detail(@PathVariable("productId") String productId,Model model) {
        model.addAttribute("product", pgProductmngService.findByRecord(productId));
        model.addAttribute("category", pgcategoryService.findAll());
        return "/detail";
    }

    @PostMapping("/detail/{productId}")
    public String delete(@PathVariable("productId") String productId,Model model) {
        model.addAttribute("product", pgProductmngService.findByRecord(productId));
        model.addAttribute("category", pgcategoryService.findAll());
        model.addAttribute("delete", pgProductmngService.delete(productId));
        model.addAttribute("complete", "削除に成功しました");
        return "success";
    }

    @GetMapping("updateinput/{id}")
    public String detail2(@ModelAttribute("updatemng") productmngAdd productmngadd,@PathVariable("id")int id, Model model) {
        model.addAttribute("product", pgProductmngService.updateid(id));
        model.addAttribute("category", pgcategoryService.findAll());
        var product = pgProductmngService.updateid(id);
        productmngadd.setProductName(product.name());
        productmngadd.setCategoryId(product.categoryId());
        productmngadd.setProductId(product.productId());
        productmngadd.setProductPrice(product.price());
        productmngadd.setDescription(product.description());

        return "updateinput";
    }

    @PostMapping("updateinput/{id}")
    public String update(@Validated @ModelAttribute("updatemng") productmngAdd productmngadd,BindingResult bindingResult,@PathVariable("id")int id,  Model model){
        System.out.println("aaaaa");
        var result3 = pgProductmngService.updateid(id);
        var result4 = pgProductmngService.findByRecord(productmngadd.getProductId());
        if (bindingResult.hasErrors()) {
            return "updateinput";
        }else if (result4 ==null || result3.productId().equals(productmngadd.getProductId())) {
            pgProductmngService.update(
                    new updateRecord(
                            id,
                            productmngadd.getProductId(),
                            productmngadd.getProductName(),
                            productmngadd.getProductPrice(),
                            productmngadd.getCategoryId(),
                            productmngadd.getDescription()
                    )
            );
            model.addAttribute("complete", "登録に成功しました");
            return "success";
        } else {
            model.addAttribute("errorupdate", "商品コードが重複しています");
//            model.addAttribute("category", pgcategoryService.findAll());
            return "updateinput";
        }
    }
}


