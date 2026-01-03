package com.recorday.recorday.frame.entity;

import com.recorday.recorday.frame.enums.ComponentType;
import com.recorday.recorday.util.entity.BasePublicIdEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@Column(nullable = false, length = 1024)
	private String source;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ComponentType type;

	private double x;
	private double y;
	private Double width;
	private Double height;
	private Double scale;
	private double rotation;
	private int zIndex;

	@Column(columnDefinition = "json")
	private String styleJson;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "frame_id")
	private Frame frame;

	// 연관관계 편의 메서드
	public void assignFrame(Frame frame) {
		this.frame = frame;
	}
}
