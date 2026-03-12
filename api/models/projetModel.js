const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Projet = sequelize.define('projet', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    nom: {
        type: DataTypes.STRING(255),
        allowNull: false
    },
    objectif: {
        type: DataTypes.TEXT,
        allowNull: false
    },
    date_debut: {
        type: DataTypes.DATE,
        allowNull: false
    },
    date_fin: {
        type: DataTypes.DATE,
        allowNull: false
    },
    valide: {
        type: DataTypes.BOOLEAN,
        defaultValue: false
    }
}, {
    timestamps: true, //created_at
    underscored: true
})

module.exports = Projet