const POKEMON_AVATAR_COUNT = 151;
const POKEMON_AVATAR_PATH = "/pokemon-avatars/";
const POKEMON_AVATAR_EXTENSION = ".webp";
const DEFAULT_STORAGE_KEY = "pokemonAvatar";

function padPokemonId(id) {
  return String(id).padStart(3, "0");
}

function hashSeed(seed) {
  const text = String(seed || Date.now());
  let hash = 0;
  for (let i = 0; i < text.length; i++) {
    hash = (hash << 5) - hash + text.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

export function getPokemonAvatarBySeed(seed) {
  const index = (hashSeed(seed) % POKEMON_AVATAR_COUNT) + 1;
  return `${POKEMON_AVATAR_PATH}${padPokemonId(index)}${POKEMON_AVATAR_EXTENSION}`;
}

export function getPokemonAvatarById(id) {
  const avatarId = Number(id);
  if (
    !Number.isInteger(avatarId) ||
    avatarId < 1 ||
    avatarId > POKEMON_AVATAR_COUNT
  ) {
    return getPokemonAvatarBySeed(id);
  }
  return `${POKEMON_AVATAR_PATH}${padPokemonId(avatarId)}${POKEMON_AVATAR_EXTENSION}`;
}

export function isPokemonAvatar(avatar) {
  return String(avatar || "").indexOf(POKEMON_AVATAR_PATH) === 0;
}

export function normalizePokemonAvatar(avatar) {
  const avatarText = String(avatar || "");
  const match = avatarText.match(/^\/pokemon-avatars\/(\d{3})\.(png|webp)$/);
  if (!match) {
    return avatar;
  }
  return getPokemonAvatarById(Number(match[1]));
}

export function getPersistentPokemonAvatar(storageKey = DEFAULT_STORAGE_KEY) {
  const storedAvatar = window.localStorage.getItem(storageKey);
  if (storedAvatar) {
    const normalizedAvatar = normalizePokemonAvatar(storedAvatar);
    if (normalizedAvatar !== storedAvatar) {
      window.localStorage.setItem(storageKey, normalizedAvatar);
    }
    return normalizedAvatar;
  }
  const randomSeed = `${Date.now()}-${Math.random()}`;
  const avatar = getPokemonAvatarBySeed(randomSeed);
  window.localStorage.setItem(storageKey, avatar);
  return avatar;
}
