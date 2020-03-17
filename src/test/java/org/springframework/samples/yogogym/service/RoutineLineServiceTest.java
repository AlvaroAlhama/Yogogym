package org.springframework.samples.yogogym.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.yogogym.model.Exercise;
import org.springframework.samples.yogogym.model.Routine;
import org.springframework.samples.yogogym.model.RoutineLine;
import org.springframework.stereotype.Service;

@DataJpaTest(includeFilters = @ComponentScan.Filter(Service.class))
public class RoutineLineServiceTest {
	
	@Autowired
	protected RoutineService routineService;
	@Autowired
	protected TrainingService trainingService;
	@Autowired
	protected RoutineLineService routineLineService;
	@Autowired
	protected ExerciseService exerciseService;
	
	@Test
	void shouldFindRoutineLineById(){
		RoutineLine routineLine = this.routineLineService.findRoutineLineById(1);
			
		Boolean notNull = routineLine != null;
		Boolean repsOrTimeNotEmptyAndGreaterThanMin = (routineLine.getReps() != null && routineLine.getReps() > 0) || (routineLine.getTime() != null && routineLine.getTime() >= 0);
		Boolean seriesNotNull= routineLine.getSeries() != null && routineLine.getSeries() >= 0; 
		Boolean weightNotNull = routineLine.getWeight() != null && routineLine.getWeight() >= 0;
		
		Boolean sameRepAndTime = routineLine.getReps() == null && routineLine.getTime().equals(2.0);
		Boolean sameSeries = routineLine.getSeries().equals(1);
		Boolean sameWeight = routineLine.getWeight().equals(0.0);
		
		assertThat(notNull && repsOrTimeNotEmptyAndGreaterThanMin && seriesNotNull && weightNotNull && sameRepAndTime && sameSeries && sameWeight);
	}
	
	@Test
	void shouldCreateRoutineLine(){
		
		final int routineId = 1;
		final int exerciseId = 1;
		
		Collection<RoutineLine> beforeAdding = this.routineLineService.findAllRoutinesLines();
		Routine routine = this.routineService.findRoutineById(routineId);
		Exercise exercise = this.exerciseService.findExerciseById(exerciseId);
		
		Collection<RoutineLine> rlFromRoutineBeforeAdding = routine.getRoutineLine();
		
		RoutineLine routineLine = new RoutineLine();
		routineLine.setReps(10);
		routineLine.setTime(null);
		routineLine.setSeries(5);
		routineLine.setWeight(5.0);
		routineLine.setExercise(exercise);
		
		routine.getRoutineLine().add(routineLine);
		this.routineService.saveRoutine(routine);
				
		Collection<RoutineLine> afterAdding = this.routineLineService.findAllRoutinesLines();
		Collection<RoutineLine> rlFromRoutineAfterAdding = this.routineService.findRoutineById(routineId).getRoutineLine();
		
		Boolean hasBeenAdded = beforeAdding.size() < afterAdding.size();
		Boolean hasBeenAddedToRoutine = rlFromRoutineBeforeAdding.size() < rlFromRoutineAfterAdding.size();
		
		RoutineLine addedRoutineLine = new ArrayList<>(afterAdding).get(afterAdding.size()-1);
		
		Boolean sameReps = routineLine.getReps().equals(addedRoutineLine.getReps());
		Boolean sameTime = routineLine.getTime() == addedRoutineLine.getTime();
		Boolean sameSeries = routineLine.getSeries().equals(addedRoutineLine.getSeries());
		Boolean sameWeight = routineLine.getWeight().equals(addedRoutineLine.getWeight());
		Boolean sameExercise = routineLine.getExercise().equals(addedRoutineLine.getExercise());
		
		assertThat(hasBeenAdded && hasBeenAddedToRoutine && sameReps && sameTime && sameSeries && sameWeight && sameExercise);
	}
	
	@Test
	void shouldDeleteRoutineLine(){
		
		final int routineLineId = 1;
		
		RoutineLine routineLine = this.routineLineService.findRoutineLineById(routineLineId);
		
		Collection<RoutineLine> beforeDelete = this.routineLineService.findAllRoutinesLines();
		
		this.routineLineService.deleteRoutineLine(routineLine);
		
		Collection<RoutineLine> afterDelete = this.routineLineService.findAllRoutinesLines();
		
		Boolean hasBeenDeleted = beforeDelete.size() > afterDelete.size();
		Boolean notExist = this.routineLineService.findRoutineLineById(routineLineId) == null;
		
		assertThat(notExist && hasBeenDeleted);
	}
}
