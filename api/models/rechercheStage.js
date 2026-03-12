const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const RechercheStage = sequelize.define('recherche_stage', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    nom_entreprise: {
        type: DataTypes.STRING(255),
        allowNull: false
    },
    nom_contact: {
        type: DataTypes.STRING(255),
        allowNull: true
    },
    email_contact: {
        type: DataTypes.STRING(255),
        allowNull: true,
        validate: {
            isEmail: true  
        }
    },
    nb_lettres_envoyees: {
        type: DataTypes.INTEGER,
        defaultValue: 0
    },
    nb_lettres_recues: {
        type: DataTypes.INTEGER,
        defaultValue: 0
    },
    date_entretien: {
        type: DataTypes.DATE,
        allowNull: true
    },
    resultat: {
        type: DataTypes.TEXT,
        allowNull: true
    },
    statut: {
        type: DataTypes.ENUM('non_contacte', 'en_attente', 'refuse', 'entretien_accorde', 'entretien_refuse', 'valide'),
        defaultValue: 'non_contacte'
    }

}, {
    timestamps: true, //created_at
    underscored: true
})

module.exports = RechercheStage