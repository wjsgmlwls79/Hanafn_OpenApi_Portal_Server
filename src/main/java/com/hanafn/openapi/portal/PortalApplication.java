package com.hanafn.openapi.portal;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import com.hanafn.openapi.portal.file.FileUploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@SpringBootApplication
@PropertySource("application.properties")
@EnableConfigurationProperties({
		FileUploadProperties.class
})
@EntityScan(basePackageClasses = {
		PortalApplication.class,
		Jsr310JpaConverters.class
})
public class PortalApplication {
	@PostConstruct
	void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}

	public static void main(String[] args) {
		SpringApplication.run(PortalApplication.class, args);
	}


}
