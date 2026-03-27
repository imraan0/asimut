const Utilisateur    = require('./utilisateur')
const Professeur     = require('./professeur')
const Secretariat    = require('./secretariat')
const Proviseur      = require('./proviseur')
const Niveau         = require('./niveau')
const Classe         = require('./classe')
const Eleve          = require('./eleve')
const Parent         = require('./parent')
const Semestre       = require('./semestre')
const Moyenne        = require('./moyenne')
const RechercheStage = require('./rechercheStage')
const Convention     = require('./convention')
const Attestation    = require('./attestation')
const Projet         = require('./projet')
const Participation  = require('./participation')
const Option         = require('./option')

// ── Authentification ─────────────────────────────────────────
// Un utilisateur est lié à un seul profil
Utilisateur.hasOne(Eleve,        { foreignKey: 'utilisateur_id' })
Eleve.belongsTo(Utilisateur,     { foreignKey: 'utilisateur_id' })

Utilisateur.hasOne(Professeur,   { foreignKey: 'utilisateur_id' })
Professeur.belongsTo(Utilisateur,{ foreignKey: 'utilisateur_id' })

Utilisateur.hasOne(Secretariat,  { foreignKey: 'utilisateur_id' })
Secretariat.belongsTo(Utilisateur,{ foreignKey: 'utilisateur_id' })

Utilisateur.hasOne(Proviseur,    { foreignKey: 'utilisateur_id' })
Proviseur.belongsTo(Utilisateur, { foreignKey: 'utilisateur_id' })

// ── Scolarité ─────────────────────────────────────────────────
// Un niveau a plusieurs classes
Niveau.hasMany(Classe,           { foreignKey: 'niveau_id' })
Classe.belongsTo(Niveau,         { foreignKey: 'niveau_id' })

// Une classe a plusieurs élèves
Classe.hasMany(Eleve,            { foreignKey: 'classe_id' })
Eleve.belongsTo(Classe,          { foreignKey: 'classe_id' })

// Un professeur est référent de plusieurs élèves
Professeur.hasMany(Eleve,        { foreignKey: 'professeur_id', as: 'eleves_referents' })
Eleve.belongsTo(Professeur,      { foreignKey: 'professeur_id', as: 'referent' })

// Un élève a un seul parent
Eleve.hasOne(Parent,             { foreignKey: 'eleve_id' })
Parent.belongsTo(Eleve,          { foreignKey: 'eleve_id' })

// ── Moyennes ──────────────────────────────────────────────────
// Un élève a plusieurs moyennes
Eleve.hasMany(Moyenne,           { foreignKey: 'eleve_id' })
Moyenne.belongsTo(Eleve,         { foreignKey: 'eleve_id' })

// Un semestre a plusieurs moyennes
Semestre.hasMany(Moyenne,        { foreignKey: 'semestre_id' })
Moyenne.belongsTo(Semestre,      { foreignKey: 'semestre_id' })

// ── Stages ────────────────────────────────────────────────────
// Un élève a plusieurs recherches de stage
Eleve.hasMany(RechercheStage,    { foreignKey: 'eleve_id' })
RechercheStage.belongsTo(Eleve,  { foreignKey: 'eleve_id' })

// Un élève a plusieurs conventions
Eleve.hasMany(Convention,        { foreignKey: 'eleve_id' })
Convention.belongsTo(Eleve,      { foreignKey: 'eleve_id' })

// Une convention a une seule attestation
Convention.hasOne(Attestation,   { foreignKey: 'convention_id' })
Attestation.belongsTo(Convention,{ foreignKey: 'convention_id' })

// Un élève a plusieurs conventions
Convention.belongsTo(Eleve, { foreignKey: 'eleve_id' });
Eleve.hasMany(Convention, { foreignKey: 'eleve_id' });

// Une convention est liée à une recherche de stage
Convention.belongsTo(RechercheStage, { foreignKey: 'recherche_stage_id' });
RechercheStage.hasOne(Convention, { foreignKey: 'recherche_stage_id' });

// ── Projets ───────────────────────────────────────────────────
// Un élève participe à plusieurs projets via Participation
Eleve.hasMany(Participation,     { foreignKey: 'eleve_id' })
Participation.belongsTo(Eleve,   { foreignKey: 'eleve_id' })

Projet.hasMany(Participation,    { foreignKey: 'projet_id' })
Participation.belongsTo(Projet,  { foreignKey: 'projet_id' })

// ── Options ───────────────────────────────────────────────────
// Many-to-many : un élève a plusieurs options, une option a plusieurs élèves
Eleve.belongsToMany(Option,      { through: 'eleve_option', foreignKey: 'eleve_id', timestamps: false })
Option.belongsToMany(Eleve,      { through: 'eleve_option', foreignKey: 'option_id', timestamps: false })

// ── Export de tous les modèles ────────────────────────────────
module.exports = {
    Utilisateur,
    Professeur,
    Secretariat,
    Proviseur,
    Niveau,
    Classe,
    Eleve,
    Parent,
    Semestre,
    Moyenne,
    RechercheStage,
    Convention,
    Attestation,
    Projet,
    Participation,
    Option
}