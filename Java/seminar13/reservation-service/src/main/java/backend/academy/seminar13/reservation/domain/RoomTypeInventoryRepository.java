package backend.academy.seminar13.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomTypeInventoryRepository {

    private final BeanPropertyRowMapper<RoomTypeInventory> mapper =
        BeanPropertyRowMapper.newInstance(RoomTypeInventory.class);

    private final JdbcClient jdbcClient;

    public int save(long hotelId, long roomTypeId, LocalDate date, int totalInventory) {
        return jdbcClient.sql("""
                insert into room_type_inventory (hotel_id, room_type_id, date, total_inventory)
                values (:hotelId, :roomTypeId, :date, :totalInventory)""")
            .param("hotelId", hotelId)
            .param("roomTypeId", roomTypeId)
            .param("date", date)
            .param("totalInventory", totalInventory)
            .update();
    }

    public int updateReserved(
        long hotelId,
        long roomTypeId,
        LocalDate date,
        int reserved
    ) {
        return jdbcClient.sql("""
                update room_type_inventory
                set total_reserved = :reserved
                where hotel_id = :hotelId
                  and room_type_id = :roomTypeId
                  and date = :date""")
            .param("hotelId", hotelId)
            .param("roomTypeId", roomTypeId)
            .param("date", date)
            .param("reserved", reserved)
            .update();
    }

    public List<RoomTypeInventory> findAllByHotelIdAndRoomTypeId(
        long hotelId,
        long roomTypeId,
        LocalDate dateFrom,
        LocalDate dateTo
    ) {
        return jdbcClient.sql("""
                select * from room_type_inventory
                         where hotel_id = :hotelId
                           and room_type_id = :roomTypeId
                           and date >= :dateFrom
                           and date <= :dateTo""")
            .param("hotelId", hotelId)
            .param("roomTypeId", roomTypeId)
            .param("dateFrom", dateFrom)
            .param("dateTo", dateTo)
            .query(mapper)
            .list();
    }

    public List<RoomTypeInventory> findAll() {
        return jdbcClient.sql("select * from room_type_inventory")
            .query(mapper)
            .list();
    }

    public List<RoomTypeInventory> findAllStartingFrom(LocalDate date) {
        return jdbcClient.sql("select * from room_type_inventory where date >= :today")
            .param("today", date)
            .query(mapper)
            .list();
    }
}
