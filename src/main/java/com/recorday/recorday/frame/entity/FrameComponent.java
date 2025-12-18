package com.recorday.recorday.frame.entity;

import com.recorday.recorday.frame.component.BackgroundConverter;
import com.recorday.recorday.frame.dto.request.BackgroundAttributes;
import com.recorday.recorday.util.entity.BasePublicIdEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "frame_component")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FrameComponent extends BasePublicIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "frame_component_id")
	private Long id;

	@Convert(converter = BackgroundConverter.class)
	@Column(columnDefinition = "json", nullable = false)
	private BackgroundAttributes background;

	@Column(nullable = false, length = 1024)
	private String source;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "frame_id")
	private Frame frame;

	// 연관관계 편의 메서드
	public void assignFrame(Frame frame) {
		this.frame = frame;
	}
}
