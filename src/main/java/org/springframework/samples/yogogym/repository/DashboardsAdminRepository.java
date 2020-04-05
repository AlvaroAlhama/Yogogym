package org.springframework.samples.yogogym.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.yogogym.model.DashboardAdmin;

public interface DashboardsAdminRepository extends CrudRepository<DashboardAdmin, Integer> {

	/* Equipment control */

	@Query("SELECT count(rl.exercise.equipment) FROM Training t left join t.routines r left join r.routineLine rl WHERE :init <= t.initialDate GROUP BY rl.exercise.equipment ORDER BY rl.exercise.equipment.name")
	List<Integer> countEquipment(@Param("init") Date init);

	@Query("SELECT rl.exercise.equipment.name FROM Training t left join t.routines r left join r.routineLine rl WHERE :init <= t.initialDate GROUP BY rl.exercise.equipment ORDER BY rl.exercise.equipment.name")
	List<String> nameEquipment(@Param("init") Date init);

}
