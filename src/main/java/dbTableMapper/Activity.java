package dbTableMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.Duration;
import lombok.*;
import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // generazione builder fluente per costruire oggetto
@Entity
@Table(name="activity")
public class Activity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
	@Setter(AccessLevel.NONE)
    private Long id;
	
	@NonNull
	private String nome;
	
	private String descrizione;
	
	private LocalDateTime inizio;
	
	private LocalDate fine_prev; // default = inizio
	
	private LocalDateTime fine;
	
	@Setter(AccessLevel.NONE)
	private double ore; // fine - inizio, default = null
	
	@Builder.Default
	private boolean done = false;
	
	@NonNull
	private String priority;
	
	@PrePersist
	@PreUpdate
	private void setDefaults() {
		if (fine_prev == null) fine_prev = inizio.toLocalDate();
		/*if (fine != null) {
			ore = Duration.between(inizio, fine).toSeconds()/3600.0;
		} else {
			ore = 0;
		}*/
	}
	
	public boolean isDone() {
		return this.done;
	}
	
	public void setOre() {
		this.ore = fine != null ? Duration.between(inizio, fine).toSeconds()/3600.0 : 0.0;
	}
	
}
