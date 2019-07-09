package com.bi.geek.companyservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@SpringBootApplication
@EnableFeignClients
public class CompanyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CompanyServiceApplication.class, args);
	}

}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
@Builder
class Company {
	@Id
	String id;

	String name;

}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CompanyDto {
	String id;
	String name;
	List<User> users;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class User {
	String id;
	String name;

}


interface CompanyRepository extends MongoRepository<Company, String> {

}

@FeignClient(name = "user-service")
interface UserClient {
	@GetMapping("/")
	List<User> getAll();

	@GetMapping("/{company}")
	List<User> getByCompany(@PathVariable("company") String company);

	@Component
	@Slf4j
	class HystrixClientFallback implements UserClient {

		@Override
		public List<User> getAll() {
			log.error("Error getAll");
			throw new RuntimeException();
		}

		@Override
		public List<User> getByCompany(String company) {
			log.error("Error getByCompany with company {}", company);
			return Collections.emptyList();
		}
	}
}

@RestController
class CompanyController {

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	UserClient userClient;

	@PostConstruct
	void init(){
		companyRepository.deleteAll();
		companyRepository.save(Company.builder().name("BiGeek").build());
		companyRepository.save(Company.builder().name("Mirai").build());
		companyRepository.save(Company.builder().name("Tadaima").build());
	}


	@GetMapping("/")
	List<CompanyDto> getAll() {

		List<Company> allCompanies = companyRepository.findAll();

		return allCompanies
				.parallelStream()
				.map(company -> {
					CompanyDto companyDto = CompanyDto.builder().id(company.getId()).name(company.getName()).build();
					List<User> usersByCompany = userClient.getByCompany(company.getName());
					companyDto.setUsers(usersByCompany);
					return companyDto;
				})
				.collect(Collectors.toList());

	}


}
