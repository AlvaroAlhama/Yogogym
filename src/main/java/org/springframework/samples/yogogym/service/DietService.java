/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.yogogym.service;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.yogogym.model.Diet;
import org.springframework.samples.yogogym.model.Training;
import org.springframework.samples.yogogym.repository.DietRepository;
import org.springframework.samples.yogogym.service.exceptions.TrainingFinished;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mostly used as a facade for all Petclinic controllers Also a placeholder
 * for @Transactional and @Cacheable annotations
 *
 * @author Michael Isvy
 */
@Service
public class DietService {

	private DietRepository dietRepository;
	private TrainingService trainingService;

	@Autowired
	public DietService(DietRepository dietRepository,TrainingService trainingService) {
		this.dietRepository = dietRepository;
		this.trainingService = trainingService;
	}
	
	@Transactional
	public void saveDiet(Diet diet, int trainingId) throws TrainingFinished{
		
		Training training = this.trainingService.findTrainingById(trainingId);
		
		Calendar cal = Calendar.getInstance();
		Date actualDate = cal.getTime();
		
		if(training.getEndDate().before(actualDate))
			throw new TrainingFinished();
		else		
			dietRepository.save(diet);
	}
	
	@Transactional
	public Collection<Diet> findAllDiet() throws DataAccessException {
		
		Collection<Diet> res = new ArrayList<>();
		
		for(Diet d: this.dietRepository.findAll())
			res.add(d);
		
		return res;		
	}
	@Transactional
	public Diet findDietById(Integer dietId) throws DataAccessException {
		
		Diet res = this.dietRepository.findById(dietId).get();
		
		return res;		
	}
}
