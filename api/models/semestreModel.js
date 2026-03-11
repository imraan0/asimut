const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Semestre = sequelize.define('semestre', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    numero:{
        type: DataTypes.INTEGER,
        allowNull: false
    },
    annee_scolaire:{
        type: DataTypes.STRING(9),
        allowNull: false
    }
}, {
    timestamps: false,
    underscored: true,
    indexes: [
        {
            unique: true,
            fields: ['numero', 'annee_scolaire']
        }
    ]
})

module.exports = Semestre