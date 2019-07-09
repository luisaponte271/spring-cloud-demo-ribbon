package com.bi.geek;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;


@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}

@RestController
class UserController {

	@Autowired
	UserRepository userRepository;

	@PostConstruct
	void init() {
		userRepository.deleteAll();
		userRepository.save(User.builder().name("Rafa").company("BiGeek").build());
		userRepository.save(User.builder().name("Javi").company("BiGeek").build());
		userRepository.save(User.builder().name("Victor").company("Mirai").build());
		userRepository.save(User.builder().name("Alvaro").company("Tadaima").build());
	}

	@GetMapping("/")
	public List<User> getAll() {
		return userRepository.findAll();
	}

	@GetMapping("/{company}")
	public List<User> getByCompany(@PathVariable("company") String company) {
		return userRepository.findByCompany(company);
	}


}

@Repository
interface UserRepository extends MongoRepository<User, String> {

	List<User> findByCompany(String company);
}


@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class User {

	@Id
	String id;

	String name;

	String company;
}
