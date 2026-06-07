<template>
  <div ref="stage" class="home-three-scene" aria-hidden="true"></div>
</template>

<script>
import * as THREE from "three";

const FLOW_COUNT = 7;
const FLOW_SEGMENTS = 96;
const PARTICLE_COUNT = 170;
const COLORS = [0x00c4b6, 0x49b1f5, 0x8e8cd8, 0xffffff];

export default {
  name: "HomeThreeScene",
  mounted() {
    this.clock = new THREE.Clock();
    this.flows = [];
    this.particles = [];
    this.ripples = [];
    this.lastRippleAt = 0;
    this.pointerNdc = new THREE.Vector2();
    this.raycaster = new THREE.Raycaster();
    this.ripplePlane = new THREE.Plane(new THREE.Vector3(0, 0, 1), 0.6);
    this.ripplePoint = new THREE.Vector3();
    this.reducedMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)"
    ).matches;

    this.initScene();
    this.resize();

    window.addEventListener("resize", this.resize);
    window.addEventListener("pointermove", this.handlePointerMove, {
      passive: true
    });
    this.animate();
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.resize);
    window.removeEventListener("pointermove", this.handlePointerMove);
    cancelAnimationFrame(this.animationFrame);
    this.disposeScene();
  },
  methods: {
    initScene() {
      const stage = this.$refs.stage;

      this.scene = new THREE.Scene();
      this.camera = new THREE.PerspectiveCamera(46, 1, 0.1, 100);
      this.camera.position.set(0, 0, 7);

      this.renderer = new THREE.WebGLRenderer({
        alpha: true,
        antialias: true
      });
      this.renderer.setClearColor(0x000000, 0);
      this.renderer.domElement.className = "home-three-canvas";
      this.renderer.domElement.dataset.scene = "home-three";
      stage.appendChild(this.renderer.domElement);

      this.sceneGroup = new THREE.Group();
      this.scene.add(this.sceneGroup);

      this.createFlowLines();
      this.createParticles();
      this.createGlassSlices();
    },
    createFlowLines() {
      for (let i = 0; i < FLOW_COUNT; i++) {
        const positions = new Float32Array(FLOW_SEGMENTS * 3);
        const geometry = new THREE.BufferGeometry();
        geometry.setAttribute(
          "position",
          new THREE.BufferAttribute(positions, 3)
        );

        const material = new THREE.LineBasicMaterial({
          color: COLORS[i % COLORS.length],
          transparent: true,
          opacity: i % 2 === 0 ? 0.42 : 0.28
        });
        const line = new THREE.Line(geometry, material);
        const flow = {
          line,
          positions,
          offset: i * 0.76,
          y: -1.85 + i * 0.55,
          z: -1.4 + i * 0.28,
          amplitude: 0.16 + i * 0.026
        };
        this.flows.push(flow);
        this.sceneGroup.add(line);
      }
    },
    createParticles() {
      const positions = new Float32Array(PARTICLE_COUNT * 3);
      const geometry = new THREE.BufferGeometry();
      geometry.setAttribute(
        "position",
        new THREE.BufferAttribute(positions, 3)
      );

      for (let i = 0; i < PARTICLE_COUNT; i++) {
        const x = -4.9 + Math.random() * 9.8;
        const y = -2.5 + Math.random() * 4.8;
        const z = -2.6 + Math.random() * 2.8;
        positions[i * 3] = x;
        positions[i * 3 + 1] = y;
        positions[i * 3 + 2] = z;
        this.particles.push({
          x,
          y,
          z,
          speed: 0.22 + Math.random() * 0.58,
          drift: Math.random() * Math.PI * 2
        });
      }

      const material = new THREE.PointsMaterial({
        color: 0xf7fffb,
        size: 0.022,
        transparent: true,
        opacity: 0.64,
        depthWrite: false
      });
      this.particleField = new THREE.Points(geometry, material);
      this.sceneGroup.add(this.particleField);
    },
    createGlassSlices() {
      const geometry = new THREE.PlaneGeometry(1, 1, 1, 1);
      const material = new THREE.MeshBasicMaterial({
        color: 0xffffff,
        transparent: true,
        opacity: 0.08,
        side: THREE.DoubleSide,
        depthWrite: false
      });

      this.glassSlices = [
        [-3.1, 0.68, -1.8, 1.3, 0.26, -0.2],
        [2.8, -0.25, -1.9, 1.05, 0.22, 0.22],
        [0.35, 1.55, -2.1, 1.55, 0.18, -0.12]
      ].map(([x, y, z, width, height, rotation]) => {
        const slice = new THREE.Mesh(geometry, material.clone());
        slice.position.set(x, y, z);
        slice.scale.set(width, height, 1);
        slice.rotation.z = rotation;
        this.sceneGroup.add(slice);
        return slice;
      });
    },
    handlePointerMove(event) {
      const stage = this.$refs.stage;
      if (!stage) {
        return;
      }

      const rect = stage.getBoundingClientRect();
      if (
        event.clientX < rect.left ||
        event.clientX > rect.right ||
        event.clientY < rect.top ||
        event.clientY > rect.bottom
      ) {
        return;
      }

      const x = (event.clientX - rect.left) / rect.width;
      const y = (event.clientY - rect.top) / rect.height;
      this.pointerNdc.set(x * 2 - 1, -(y * 2 - 1));
      this.raycaster.setFromCamera(this.pointerNdc, this.camera);

      if (
        !this.raycaster.ray.intersectPlane(this.ripplePlane, this.ripplePoint)
      ) {
        return;
      }

      const now = performance.now();
      if (now - this.lastRippleAt > 180) {
        this.lastRippleAt = now;
        this.addRipple(this.ripplePoint.x, this.ripplePoint.y);
      }
    },
    addRipple(x, y) {
      const geometry = new THREE.RingGeometry(0.08, 0.083, 72);
      const material = new THREE.MeshBasicMaterial({
        color: 0x9ff7e9,
        transparent: true,
        opacity: 0.48,
        side: THREE.DoubleSide,
        depthWrite: false
      });
      const ripple = new THREE.Mesh(geometry, material);
      ripple.position.set(x, y, -0.6);
      this.sceneGroup.add(ripple);
      this.ripples.push({
        mesh: ripple,
        age: 0
      });

      if (this.ripples.length > 10) {
        this.removeRipple(this.ripples.shift());
      }
    },
    resize() {
      const stage = this.$refs.stage;
      if (!stage || !this.renderer || !this.camera) {
        return;
      }

      const width = stage.clientWidth || 1;
      const height = stage.clientHeight || 1;
      this.renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
      this.renderer.setSize(width, height, false);
      this.camera.aspect = width / height;
      this.camera.position.z = width < 760 ? 8.2 : 7;
      this.camera.fov = width < 760 ? 54 : 46;
      this.camera.updateProjectionMatrix();
    },
    animate() {
      const delta = Math.min(this.clock.getDelta(), 0.05);
      const elapsed = this.clock.elapsedTime;

      this.updateFlowLines(elapsed);
      this.updateParticles(elapsed);
      this.updateRipples(delta);

      const motion = this.reducedMotion ? 0 : 1;
      this.glassSlices.forEach((slice, index) => {
        slice.rotation.z += Math.sin(elapsed * 0.4 + index) * 0.0008 * motion;
      });

      this.renderer.render(this.scene, this.camera);
      this.animationFrame = requestAnimationFrame(this.animate);
    },
    updateFlowLines(elapsed) {
      this.flows.forEach(flow => {
        for (let i = 0; i < FLOW_SEGMENTS; i++) {
          const t = i / (FLOW_SEGMENTS - 1);
          const x = -5.25 + t * 10.5;
          const wave =
            Math.sin(t * Math.PI * 2 + elapsed * 0.5 + flow.offset) *
              flow.amplitude +
            Math.sin(t * Math.PI * 5 + elapsed * 0.24 + flow.offset) * 0.05;
          flow.positions[i * 3] = x;
          flow.positions[i * 3 + 1] = flow.y + wave;
          flow.positions[i * 3 + 2] = flow.z;
        }
        flow.line.geometry.attributes.position.needsUpdate = true;
      });
    },
    updateParticles(elapsed) {
      const positions = this.particleField.geometry.attributes.position.array;
      this.particles.forEach((particle, index) => {
        const drift = elapsed * particle.speed + particle.drift;
        positions[index * 3] = particle.x + Math.sin(drift * 0.72) * 0.08;
        positions[index * 3 + 1] = particle.y + Math.cos(drift) * 0.06;
        positions[index * 3 + 2] = particle.z + Math.sin(drift * 0.4) * 0.08;
      });
      this.particleField.geometry.attributes.position.needsUpdate = true;
    },
    updateRipples(delta) {
      for (let i = this.ripples.length - 1; i >= 0; i--) {
        const ripple = this.ripples[i];
        ripple.age += delta;
        const progress = ripple.age / 1.15;
        ripple.mesh.scale.setScalar(1 + progress * 8);
        ripple.mesh.material.opacity = Math.max(0, 0.48 * (1 - progress));

        if (progress >= 1) {
          this.removeRipple(ripple);
          this.ripples.splice(i, 1);
        }
      }
    },
    removeRipple(ripple) {
      if (!ripple) {
        return;
      }
      this.sceneGroup.remove(ripple.mesh);
      ripple.mesh.geometry.dispose();
      ripple.mesh.material.dispose();
    },
    disposeScene() {
      if (!this.scene) {
        return;
      }

      this.scene.traverse(object => {
        if (object.geometry) {
          object.geometry.dispose();
        }
        if (object.material) {
          if (Array.isArray(object.material)) {
            object.material.forEach(material => material.dispose());
          } else {
            object.material.dispose();
          }
        }
      });

      if (this.renderer) {
        this.renderer.dispose();
        if (this.renderer.domElement && this.renderer.domElement.parentNode) {
          this.renderer.domElement.parentNode.removeChild(
            this.renderer.domElement
          );
        }
      }
    }
  }
};
</script>

<style scoped>
.home-three-scene {
  position: absolute;
  inset: 0;
  z-index: 1;
  overflow: hidden;
  pointer-events: none;
}

.home-three-scene::before {
  position: absolute;
  inset: 0;
  content: "";
  background: rgba(4, 13, 20, 0.2);
}

.home-three-scene :deep(.home-three-canvas) {
  position: relative;
  width: 100%;
  height: 100%;
  display: block;
}

@media (max-width: 759px) {
  .home-three-scene {
    opacity: 0.78;
  }
}
</style>
