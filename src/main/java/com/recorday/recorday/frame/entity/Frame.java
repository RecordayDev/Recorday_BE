package com.recorday.recorday.frame.entity;

import java.util.ArrayList;
import java.util.List;

import com.recorday.recorday.frame.component.BackgroundConverter;
import com.recorday.recorday.frame.entity.attributes.BackgroundAttributes;
import com.recorday.recorday.frame.enums.FrameType;
import com.recorday.recorday.user.entity.User;
import com.recorday.recorday.util.entity.BasePublicIdEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "frame")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Frame extends BasePublicIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "frame_id")
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false, length = 1024)
	private String previewKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FrameType frameType;

	private int canvasWidth;
	private int canvasHeight;

	@Convert(converter = BackgroundConverter.class)
	@Column(columnDefinition = "json", nullable = false)
	private BackgroundAttributes background;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Builder.Default
	@OneToMany(mappedBy = "frame", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FrameComponent> components = new ArrayList<>();

	// 연관관계 편의 메서드
	public void assignUser(User user) {
		this.user = user;
	}

	public void addComponent(FrameComponent component) {
		this.components.add(component);
		component.assignFrame(this);
	}

	public void updateMetadata(String title, String description, int canvasWidth, int canvasHeight, BackgroundAttributes background, String previewKey) {
		this.title = title;
		this.description = description;
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.background = background;
		this.previewKey = previewKey;
	}
}
