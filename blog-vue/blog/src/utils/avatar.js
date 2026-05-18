const POKEMON_AVATAR_COUNT = 151;
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
  return `/pokemon-avatars/${padPokemonId(index)}.png`;
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
  return `/pokemon-avatars/${padPokemonId(avatarId)}.png`;
}

export function isPokemonAvatar(avatar) {
  return String(avatar || "").indexOf("/pokemon-avatars/") === 0;
}

export function getPersistentPokemonAvatar(storageKey = DEFAULT_STORAGE_KEY) {
  const storedAvatar = window.localStorage.getItem(storageKey);
  if (storedAvatar) {
    return storedAvatar;
  }
  const randomSeed = `${Date.now()}-${Math.random()}`;
  const avatar = getPokemonAvatarBySeed(randomSeed);
  window.localStorage.setItem(storageKey, avatar);
  return avatar;
}
