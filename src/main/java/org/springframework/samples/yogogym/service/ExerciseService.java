/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.yogogym.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.yogogym.model.Exercise;
import org.springframework.samples.yogogym.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mostly used as a facade for all Petclinic controllers Also a placeholder
 * for @Transactional and @Cacheable annotations
 *
 * @author Michael Isvy
 */
@Service
public class ExerciseService {

	private ExerciseRepository exerciseRepository;


	@Autowired
	public ExerciseService(final ExerciseRepository exerciseRepository) {
		this.exerciseRepository = exerciseRepository;
	}

	@Transactional
	public void saveExercise(final Exercise exercise) throws DataAccessException {
		this.exerciseRepository.save(exercise);
	}

	@Transactional
	public Collection<Exercise> findAllExercise() throws DataAccessException {

		Collection<Exercise> res = new ArrayList<>();

		for (Exercise e : this.exerciseRepository.findAll()) {
			res.add(e);
		}

		return res;
	}

	@Transactional(readOnly = true)
	public Exercise findExerciseById(final int id) throws DataAccessException {
		return this.exerciseRepository.findById(id);
	}
}
